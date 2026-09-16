import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getProductos, deleteProducto } from '../services/productosService';
import { getInventarios, deleteInventario } from '../services/inventarioService';
import { getCategorias } from '../services/categoriasService';
import type { Categoria, UnifiedProduct } from '../models/types';
import {
  getIdProducto,
  getIdCategoria,
  getIdInventario,
  getUnidadMedida,
} from '../models/types';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ConfirmDialog } from '../components/ConfirmDialog';

interface InventoryPageProps {
  showToast: (text: string, type?: 'success' | 'error' | 'info') => void;
}

export const InventoryPage: React.FC<InventoryPageProps> = ({ showToast }) => {
  const [loading, setLoading] = useState(true);
  const [items, setItems] = useState<UnifiedProduct[]>([]);
  const [categorias, setCategorias] = useState<Categoria[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategoria, setSelectedCategoria] = useState<number | 'all'>('all');

  // Modal confirm state
  const [deleteTarget, setDeleteTarget] = useState<UnifiedProduct | null>(null);

  const loadData = async () => {
    try {
      setLoading(true);
      const [prodRes, invRes, catRes] = await Promise.all([
        getProductos(),
        getInventarios(),
        getCategorias(),
      ]);

      const catMap = new Map<number, string>();
      catRes.forEach((c) => {
        const catId = getIdCategoria(c);
        if (catId) catMap.set(catId, c.nombre);
      });

      // Combine products and inventory with robust fallback getters
      const unifiedList: UnifiedProduct[] = prodRes.map((p) => {
        const pId = getIdProducto(p);
        const cId = getIdCategoria(p);

        // Find inventory matching by id_producto or idProducto
        const inv = invRes.find((i) => getIdProducto(i) === pId);

        return {
          id_producto: pId,
          nombre: p.nombre,
          descripcion: p.descripcion,
          precio_estimado: p.precio_estimado,
          id_categoria: cId,
          nombre_categoria: catMap.get(cId) || p.categoria?.nombre || 'General',
          id_inventario: inv ? getIdInventario(inv) : undefined,
          cantidad: inv ? inv.cantidad : 0,
          cantidad_minima: inv ? (inv.cantidadMinima ?? inv.cantidad_minima) : 2,
          unidad_medida: inv ? getUnidadMedida(inv) : getUnidadMedida(p),
          ubicacion: inv?.ubicacion,
          fecha_vencimiento: inv ? (inv.fechaVencimiento ?? inv.fecha_vencimiento) : null,
          fecha_registro: inv ? (inv.fechaIngreso ?? inv.fecha_registro) : null,
        };
      });

      setItems(unifiedList);
      setCategorias(catRes);
    } catch (err) {
      console.error('Error al cargar inventario:', err);
      showToast('Error al conectar con la API de SQL Server', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleDeleteConfirm = async () => {
    if (!deleteTarget) return;

    try {
      if (deleteTarget.id_inventario) {
        await deleteInventario(deleteTarget.id_inventario);
      }
      await deleteProducto(deleteTarget.id_producto);

      showToast(`Producto "${deleteTarget.nombre}" eliminado correctamente.`, 'success');
      setItems((prev) => prev.filter((i) => i.id_producto !== deleteTarget.id_producto));
    } catch (err) {
      console.error('Error al eliminar producto:', err);
      showToast('No se pudo eliminar el producto. Verifica dependencias.', 'error');
    } finally {
      setDeleteTarget(null);
    }
  };

  // Filtered items
  const filteredItems = items.filter((item) => {
    const matchesSearch = item.nombre.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (item.descripcion && item.descripcion.toLowerCase().includes(searchQuery.toLowerCase()));
    
    const matchesCat = selectedCategoria === 'all' || item.id_categoria === Number(selectedCategoria);

    return matchesSearch && matchesCat;
  });

  const getExpirationBadge = (dateStr?: string | null) => {
    if (!dateStr) return null;
    const vDate = new Date(dateStr);
    const now = new Date();
    const diffDays = Math.ceil((vDate.getTime() - now.getTime()) / (1000 * 3600 * 24));

    if (diffDays < 0) {
      return <span className="badge badge-danger">Vencido ({Math.abs(diffDays)}d)</span>;
    } else if (diffDays <= 7) {
      return <span className="badge badge-warning">Vence en {diffDays}d</span>;
    } else {
      return <span className="badge badge-success">Vence: {vDate.toLocaleDateString()}</span>;
    }
  };

  if (loading) {
    return <LoadingSpinner message="Cargando catálogo e inventario..." />;
  }

  return (
    <div className="page-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
        <div>
          <h1 className="page-title">Inventario</h1>
          <p className="page-subtitle">Productos en despensa ({filteredItems.length})</p>
        </div>
        <Link to="/agregar" className="btn btn-primary" style={{ padding: '8px 14px', fontSize: '0.85rem' }}>
          ➕ Nuevo
        </Link>
      </div>

      {/* Filters */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginBottom: '20px' }}>
        <input
          type="text"
          className="form-input"
          placeholder="🔍 Buscar por nombre..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
        
        <select
          className="form-select"
          value={selectedCategoria}
          onChange={(e) => setSelectedCategoria(e.target.value === 'all' ? 'all' : Number(e.target.value))}
        >
          <option value="all">Todas las categorías ({categorias.length})</option>
          {categorias.map((cat) => {
            const catId = getIdCategoria(cat);
            return (
              <option key={catId} value={catId}>
                {cat.nombre}
              </option>
            );
          })}
        </select>
      </div>

      {/* Product List */}
      {filteredItems.length === 0 ? (
        <div className="empty-state glass-card">
          <div className="empty-state-icon">🥫</div>
          <p>No se encontraron productos en el inventario.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          {filteredItems.map((item) => (
            <div key={item.id_producto} className="glass-card" style={{ marginBottom: 0 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                <div>
                  <span className="badge badge-info" style={{ marginBottom: '6px' }}>
                    {item.nombre_categoria}
                  </span>
                  <h3 style={{ fontSize: '1.05rem', fontWeight: '600' }}>{item.nombre}</h3>
                  {item.descripcion && (
                    <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '2px' }}>
                      {item.descripcion}
                    </p>
                  )}
                </div>
                {getExpirationBadge(item.fecha_vencimiento)}
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '12px', paddingTop: '10px', borderTop: '1px solid rgba(255, 255, 255, 0.05)' }}>
                <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
                  <div>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block' }}>Stock</span>
                    <span style={{ fontSize: '1rem', fontWeight: '700', color: item.cantidad > 0 ? '#4ade80' : '#ef4444' }}>
                      {item.cantidad} {item.unidad_medida}
                    </span>
                  </div>
                  {item.ubicacion && (
                    <div>
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block' }}>Ubicación</span>
                      <span style={{ fontSize: '0.85rem', color: 'var(--text-main)' }}>
                        📍 {item.ubicacion}
                      </span>
                    </div>
                  )}
                </div>

                <div style={{ display: 'flex', gap: '8px' }}>
                  <Link
                    to={`/editar/${item.id_producto}`}
                    className="btn btn-secondary"
                    style={{ padding: '6px 12px', fontSize: '0.8rem' }}
                  >
                    ✏️ Editar
                  </Link>
                  <button
                    className="btn btn-danger"
                    style={{ padding: '6px 12px', fontSize: '0.8rem' }}
                    onClick={() => setDeleteTarget(item)}
                  >
                    🗑️
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Delete confirmation modal */}
      <ConfirmDialog
        isOpen={!!deleteTarget}
        title="¿Eliminar producto?"
        message={`¿Estás seguro de que deseas eliminar "${deleteTarget?.nombre}"? Esta acción eliminará el registro del catálogo e inventario en SQL Server.`}
        confirmText="Eliminar"
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
};
