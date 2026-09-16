import React, { useEffect, useState } from 'react';
import { getProductos } from '../services/productosService';
import { getInventarios } from '../services/inventarioService';
import { getCategorias } from '../services/categoriasService';
import { getListasCompras, createListaCompra } from '../services/listasComprasService';
import { createDetalle } from '../services/detalleListaService';
import type { UnifiedProduct, ListaCompra } from '../models/types';
import {
  getIdProducto,
  getIdCategoria,
  getIdInventario,
  getIdLista,
  getUnidadMedida,
} from '../models/types';
import { LoadingSpinner } from '../components/LoadingSpinner';

interface ExpiringPageProps {
  showToast: (text: string, type?: 'success' | 'error' | 'info') => void;
}

export const ExpiringPage: React.FC<ExpiringPageProps> = ({ showToast }) => {
  const [loading, setLoading] = useState(true);
  const [expiringItems, setExpiringItems] = useState<UnifiedProduct[]>([]);
  const [listas, setListas] = useState<ListaCompra[]>([]);
  const [selectedListaId, setSelectedListaId] = useState<number | 'new'>('new');
  const [addingToCart, setAddingToCart] = useState<number | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [prodRes, invRes, catRes, listasRes] = await Promise.all([
          getProductos(),
          getInventarios(),
          getCategorias(),
          getListasCompras(),
        ]);

        const catMap = new Map<number, string>();
        catRes.forEach((c) => {
          const cId = getIdCategoria(c);
          if (cId) catMap.set(cId, c.nombre);
        });

        const now = new Date();
        const next7Days = new Date();
        next7Days.setDate(now.getDate() + 7);

        // Filter expiring or expired products
        const filtered: UnifiedProduct[] = [];

        prodRes.forEach((p) => {
          const pId = getIdProducto(p);
          const cId = getIdCategoria(p);
          const inv = invRes.find((i) => getIdProducto(i) === pId);
          const fVenc = inv ? (inv.fechaVencimiento ?? inv.fecha_vencimiento) : null;

          if (inv && fVenc) {
            const vDate = new Date(fVenc);
            if (vDate <= next7Days) {
              filtered.push({
                id_producto: pId,
                nombre: p.nombre,
                descripcion: p.descripcion,
                precio_estimado: p.precio_estimado,
                id_categoria: cId,
                nombre_categoria: catMap.get(cId) || 'General',
                id_inventario: getIdInventario(inv),
                cantidad: inv.cantidad,
                unidad_medida: getUnidadMedida(inv),
                fecha_vencimiento: fVenc,
                fecha_registro: inv.fechaIngreso ?? inv.fecha_registro,
              });
            }
          }
        });

        // Sort by earliest expiration date
        filtered.sort((a, b) => new Date(a.fecha_vencimiento!).getTime() - new Date(b.fecha_vencimiento!).getTime());

        setExpiringItems(filtered);
        setListas(listasRes);
        if (listasRes.length > 0) {
          setSelectedListaId(getIdLista(listasRes[0]));
        }
      } catch (err) {
        console.error('Error al cargar productos por vencer:', err);
        showToast('Error al conectar con el servidor', 'error');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const handleQuickAddToList = async (item: UnifiedProduct) => {
    try {
      setAddingToCart(item.id_producto);
      let targetListaId: number;

      if (selectedListaId === 'new' || listas.length === 0) {
        // Create new shopping list if none selected or exists
        const newList = await createListaCompra({
          nombreLista: `Lista Alertas ${new Date().toLocaleDateString()}`,
          fechaCreacion: new Date().toISOString(),
          estado: 'PENDIENTE',
        });
        targetListaId = getIdLista(newList);
        setListas((prev) => [...prev, newList]);
        setSelectedListaId(targetListaId);
      } else {
        targetListaId = Number(selectedListaId);
      }

      // Add detail to list
      await createDetalle({
        idLista: targetListaId,
        id_lista: targetListaId,
        idProducto: item.id_producto,
        id_producto: item.id_producto,
        cantidadAComprar: item.cantidad > 0 ? item.cantidad : 1,
        cantidad: item.cantidad > 0 ? item.cantidad : 1,
        unidadMedida: item.unidad_medida || 'unidades',
        unidad_medida: item.unidad_medida || 'unidades',
        comprado: false,
      });

      showToast(`"${item.nombre}" agregado a la lista de compras.`, 'success');
    } catch (err) {
      console.error('Error al agregar a la lista:', err);
      showToast('Error al añadir producto a la lista de compras', 'error');
    } finally {
      setAddingToCart(null);
    }
  };

  if (loading) {
    return <LoadingSpinner message="Analizando fechas de vencimiento..." />;
  }

  return (
    <div className="page-container">
      <div style={{ marginBottom: '20px' }}>
        <h1 className="page-title" style={{ color: '#fdba74' }}>⚠️ Productos Por Vencer</h1>
        <p className="page-subtitle">Alimentos vencidos o a punto de vencer (Próximos 7 días)</p>
      </div>

      {listas.length > 0 && (
        <div className="glass-card" style={{ padding: '14px 18px', marginBottom: '20px' }}>
          <label className="form-label" style={{ marginBottom: '6px' }}>
            Destino para agregar a Lista de Compras:
          </label>
          <select
            className="form-select"
            value={selectedListaId}
            onChange={(e) => setSelectedListaId(e.target.value === 'new' ? 'new' : Number(e.target.value))}
          >
            {listas.map((l) => (
              <option key={l.id_lista} value={l.id_lista}>
                {l.nombre_lista} ({l.estado || 'Pendiente'})
              </option>
            ))}
            <option value="new">➕ Crear nueva lista automáticamente</option>
          </select>
        </div>
      )}

      {expiringItems.length === 0 ? (
        <div className="empty-state glass-card">
          <div className="empty-state-icon">🎉</div>
          <h3 style={{ fontSize: '1.1rem', marginBottom: '6px', color: '#4ade80' }}>¡Todo en excelente estado!</h3>
          <p>No tienes ningún producto vencido ni por vencer en los próximos 7 días.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          {expiringItems.map((item) => {
            const vDate = new Date(item.fecha_vencimiento!);
            const now = new Date();
            const diffDays = Math.ceil((vDate.getTime() - now.getTime()) / (1000 * 3600 * 24));
            const isExpired = diffDays < 0;

            return (
              <div
                key={item.id_producto}
                className="glass-card"
                style={{
                  borderLeft: isExpired ? '4px solid var(--danger)' : '4px solid var(--warning)',
                  marginBottom: 0,
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <span className="badge badge-info" style={{ marginBottom: '4px' }}>
                      {item.nombre_categoria}
                    </span>
                    <h3 style={{ fontSize: '1.05rem', fontWeight: '600' }}>{item.nombre}</h3>
                    <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      Stock actual: <strong>{item.cantidad} {item.unidad_medida}</strong>
                    </p>
                  </div>
                  <span className={`badge ${isExpired ? 'badge-danger' : 'badge-warning'}`}>
                    {isExpired ? `Vencido hace ${Math.abs(diffDays)}d` : `Vence en ${diffDays}d`}
                  </span>
                </div>

                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginTop: '14px',
                    paddingTop: '10px',
                    borderTop: '1px solid rgba(255, 255, 255, 0.05)',
                  }}
                >
                  <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                    Fecha: {vDate.toLocaleDateString()}
                  </span>

                  <button
                    className="btn btn-primary"
                    style={{ fontSize: '0.8rem', padding: '6px 12px' }}
                    onClick={() => handleQuickAddToList(item)}
                    disabled={addingToCart === item.id_producto}
                  >
                    🛒 {addingToCart === item.id_producto ? 'Añadiendo...' : 'Reponer en Lista'}
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
