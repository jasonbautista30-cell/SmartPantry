import React, { useEffect, useState } from 'react';
import {
  getListasCompras,
  createListaCompra,
  deleteListaCompra,
} from '../services/listasComprasService';
import {
  getDetalles,
  createDetalle,
  updateDetalle,
  deleteDetalle,
} from '../services/detalleListaService';
import { getProductos } from '../services/productosService';
import type { ListaCompra, DetalleListaCompra, Producto } from '../models/types';
import {
  getIdLista,
  getIdProducto,
  getIdDetalle,
  getNombreLista,
  getCantidadDetalle,
  getUnidadMedida,
} from '../models/types';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ConfirmDialog } from '../components/ConfirmDialog';

interface ShoppingListPageProps {
  showToast: (text: string, type?: 'success' | 'error' | 'info') => void;
}

export const ShoppingListPage: React.FC<ShoppingListPageProps> = ({ showToast }) => {
  const [loading, setLoading] = useState(true);
  const [listas, setListas] = useState<ListaCompra[]>([]);
  const [productos, setProductos] = useState<Producto[]>([]);
  const [detalles, setDetalles] = useState<DetalleListaCompra[]>([]);
  
  const [selectedLista, setSelectedLista] = useState<ListaCompra | null>(null);

  // New List Modal/Form
  const [showNewListForm, setShowNewListForm] = useState(false);
  const [nombreListaInput, setNombreListaInput] = useState('');

  // Add Item to List Form
  const [showAddItemForm, setShowAddItemForm] = useState(false);
  const [selectedProductoId, setSelectedProductoId] = useState<number | ''>('');
  const [cantidadInput, setCantidadInput] = useState<number>(1);
  const [unidadInput, setUnidadInput] = useState<string>('unidades');

  // Confirm Dialog
  const [deleteListTarget, setDeleteListTarget] = useState<ListaCompra | null>(null);
  const [deleteDetailTarget, setDeleteDetailTarget] = useState<DetalleListaCompra | null>(null);

  const loadAllData = async () => {
    try {
      setLoading(true);
      const [listasRes, prodsRes, detallesRes] = await Promise.all([
        getListasCompras(),
        getProductos(),
        getDetalles(),
      ]);

      setListas(listasRes);
      setProductos(prodsRes);
      setDetalles(detallesRes);

      if (listasRes.length > 0) {
        setSelectedLista(listasRes[0]);
      }
    } catch (err) {
      console.error('Error al cargar listas de compras:', err);
      showToast('Error al conectar con el servidor', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAllData();
  }, []);

  const handleCreateList = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!nombreListaInput.trim()) return;

    try {
      const newList = await createListaCompra({
        nombreLista: nombreListaInput.trim(),
        fechaCreacion: new Date().toISOString(),
        estado: 'PENDIENTE',
      });

      setListas((prev) => [...prev, newList]);
      setSelectedLista(newList);
      setNombreListaInput('');
      setShowNewListForm(false);
      showToast(`Lista "${getNombreLista(newList)}" creada.`, 'success');
    } catch (err) {
      console.error('Error al crear lista:', err);
      showToast('Error al crear lista de compras', 'error');
    }
  };

  const handleAddItem = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedLista || !selectedProductoId) return;

    try {
      const createdDetalle = await createDetalle({
        idLista: getIdLista(selectedLista),
        idProducto: Number(selectedProductoId),
        cantidadAComprar: Number(cantidadInput),
        unidadMedida: unidadInput,
        comprado: false,
      });

      setDetalles((prev) => [...prev, createdDetalle]);
      setShowAddItemForm(false);
      setSelectedProductoId('');
      showToast('Producto añadido a la lista.', 'success');
    } catch (err) {
      console.error('Error al agregar detalle:', err);
      showToast('Error al agregar item a la lista', 'error');
    }
  };

  const handleToggleComprado = async (item: DetalleListaCompra) => {
    try {
      const dId = getIdDetalle(item);
      const updatedStatus = !item.comprado;
      await updateDetalle(dId, {
        ...item,
        comprado: updatedStatus,
      });

      setDetalles((prev) =>
        prev.map((d) => (getIdDetalle(d) === dId ? { ...d, comprado: updatedStatus } : d))
      );
    } catch (err) {
      console.error('Error al cambiar estado comprado:', err);
      showToast('No se pudo actualizar el estado del item', 'error');
    }
  };

  const handleDeleteListConfirm = async () => {
    if (!deleteListTarget) return;

    try {
      const targetId = getIdLista(deleteListTarget);
      await deleteListaCompra(targetId);
      setListas((prev) => prev.filter((l) => getIdLista(l) !== targetId));
      if (selectedLista && getIdLista(selectedLista) === targetId) {
        const remaining = listas.filter((l) => getIdLista(l) !== targetId);
        setSelectedLista(remaining.length > 0 ? remaining[0] : null);
      }
      showToast('Lista de compras eliminada.', 'success');
    } catch (err) {
      console.error('Error al eliminar lista:', err);
      showToast('Error al eliminar lista. Asegúrate de vaciar sus items.', 'error');
    } finally {
      setDeleteListTarget(null);
    }
  };

  const handleDeleteDetailConfirm = async () => {
    if (!deleteDetailTarget) return;

    try {
      const targetId = getIdDetalle(deleteDetailTarget);
      await deleteDetalle(targetId);
      setDetalles((prev) => prev.filter((d) => getIdDetalle(d) !== targetId));
      showToast('Item removido de la lista.', 'success');
    } catch (err) {
      console.error('Error al eliminar detalle:', err);
      showToast('Error al eliminar item de la lista', 'error');
    } finally {
      setDeleteDetailTarget(null);
    }
  };

  if (loading) {
    return <LoadingSpinner message="Cargando listas de compras..." />;
  }

  // Items belonging to selected list
  const currentListItems = selectedLista
    ? detalles.filter((d) => {
        const dListId = d.idLista ?? d.id_lista;
        return dListId === getIdLista(selectedLista);
      })
    : [];

  return (
    <div className="page-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
        <div>
          <h1 className="page-title">Listas de Compras</h1>
          <p className="page-subtitle">Planifica tus compras de despensa</p>
        </div>
        <button
          className="btn btn-primary"
          style={{ padding: '8px 14px', fontSize: '0.85rem' }}
          onClick={() => setShowNewListForm(true)}
        >
          ➕ Nueva Lista
        </button>
      </div>

      {/* New List Form */}
      {showNewListForm && (
        <form onSubmit={handleCreateList} className="glass-card" style={{ padding: '16px', marginBottom: '20px' }}>
          <h3 style={{ fontSize: '1rem', marginBottom: '12px' }}>Crear Nueva Lista</h3>
          <div className="form-group">
            <input
              type="text"
              className="form-input"
              placeholder="Nombre de la lista (ej: Supermercado Semanal)"
              value={nombreListaInput}
              onChange={(e) => setNombreListaInput(e.target.value)}
              required
            />
          </div>
          <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
            <button
              type="button"
              className="btn btn-secondary"
              style={{ padding: '6px 12px', fontSize: '0.8rem' }}
              onClick={() => setShowNewListForm(false)}
            >
              Cancelar
            </button>
            <button type="submit" className="btn btn-primary" style={{ padding: '6px 14px', fontSize: '0.8rem' }}>
              Guardar Lista
            </button>
          </div>
        </form>
      )}

      {/* Select List Dropdown or Tabs */}
      {listas.length > 0 && (
        <div className="glass-card" style={{ padding: '14px', marginBottom: '20px' }}>
          <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
            <select
              className="form-select"
              value={selectedLista ? getIdLista(selectedLista) : ''}
              onChange={(e) => {
                const found = listas.find((l) => getIdLista(l) === Number(e.target.value));
                if (found) setSelectedLista(found);
              }}
            >
              {listas.map((l) => {
                const lId = getIdLista(l);
                return (
                  <option key={lId} value={lId}>
                    📋 {getNombreLista(l)}
                  </option>
                );
              })}
            </select>
            {selectedLista && (
              <button
                className="btn btn-danger"
                style={{ padding: '10px 14px', flexShrink: 0 }}
                onClick={() => setDeleteListTarget(selectedLista)}
                title="Eliminar lista"
              >
                🗑️
              </button>
            )}
          </div>
        </div>
      )}

      {/* Current List Content */}
      {selectedLista ? (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
            <h2 style={{ fontSize: '1.1rem', fontWeight: '600' }}>
              Items en "{getNombreLista(selectedLista)}" ({currentListItems.length})
            </h2>
            <button
              className="btn btn-secondary"
              style={{ fontSize: '0.8rem', padding: '6px 12px' }}
              onClick={() => setShowAddItemForm(true)}
            >
              ➕ Agregar Item
            </button>
          </div>

          {/* Add item form modal */}
          {showAddItemForm && (
            <form onSubmit={handleAddItem} className="glass-card" style={{ padding: '18px', marginBottom: '20px' }}>
              <h3 style={{ fontSize: '0.95rem', marginBottom: '12px' }}>Agregar Producto a la Lista</h3>
              
              <div className="form-group">
                <label className="form-label">Producto</label>
                <select
                  className="form-select"
                  value={selectedProductoId}
                  onChange={(e) => setSelectedProductoId(Number(e.target.value))}
                  required
                >
                  <option value="" disabled>Seleccione producto...</option>
                  {productos.map((p) => {
                    const pId = getIdProducto(p);
                    return (
                      <option key={pId} value={pId}>
                        {p.nombre}
                      </option>
                    );
                  })}
                </select>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                <div className="form-group">
                  <label className="form-label">Cantidad</label>
                  <input
                    type="number"
                    min="1"
                    className="form-input"
                    value={cantidadInput}
                    onChange={(e) => setCantidadInput(Number(e.target.value))}
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Unidad</label>
                  <select
                    className="form-select"
                    value={unidadInput}
                    onChange={(e) => setUnidadInput(e.target.value)}
                  >
                    <option value="unidades">Unidades</option>
                    <option value="kg">kg</option>
                    <option value="g">g</option>
                    <option value="litros">litros</option>
                    <option value="paquetes">paquetes</option>
                    <option value="latas">latas</option>
                  </select>
                </div>
              </div>

              <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end', marginTop: '12px' }}>
                <button
                  type="button"
                  className="btn btn-secondary"
                  style={{ fontSize: '0.8rem', padding: '6px 12px' }}
                  onClick={() => setShowAddItemForm(false)}
                >
                  Cancelar
                </button>
                <button type="submit" className="btn btn-primary" style={{ fontSize: '0.8rem', padding: '6px 14px' }}>
                  Añadir
                </button>
              </div>
            </form>
          )}

          {/* List items */}
          {currentListItems.length === 0 ? (
            <div className="empty-state glass-card">
              <div className="empty-state-icon">🛒</div>
              <p>Esta lista está vacía. ¡Agrega productos para comenzar!</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {currentListItems.map((item) => {
                const dId = getIdDetalle(item);
                const itemProdId = getIdProducto(item);
                const prod = productos.find((p) => getIdProducto(p) === itemProdId) || item.producto;

                return (
                  <div
                    key={dId}
                    className="glass-card"
                    style={{
                      marginBottom: 0,
                      opacity: item.comprado ? 0.6 : 1,
                      textDecoration: item.comprado ? 'line-through' : 'none',
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <input
                          type="checkbox"
                          checked={!!item.comprado}
                          onChange={() => handleToggleComprado(item)}
                          style={{ width: '20px', height: '20px', accentColor: 'var(--primary)', cursor: 'pointer' }}
                        />
                        <div>
                          <h4 style={{ fontSize: '0.98rem', fontWeight: '600' }}>
                            {prod ? prod.nombre : `Producto #${itemProdId}`}
                          </h4>
                          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                            Cantidad: {getCantidadDetalle(item)} {getUnidadMedida(item)}
                          </span>
                        </div>
                      </div>

                      <button
                        className="btn btn-secondary"
                        style={{ padding: '4px 8px', fontSize: '0.75rem' }}
                        onClick={() => setDeleteDetailTarget(item)}
                      >
                        ❌
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      ) : (
        <div className="empty-state glass-card">
          <div className="empty-state-icon">📝</div>
          <p>No hay listas de compras disponibles. Crea una nueva lista.</p>
        </div>
      )}

      {/* Delete List Modal */}
      <ConfirmDialog
        isOpen={!!deleteListTarget}
        title="¿Eliminar lista de compras?"
        message={`¿Estás seguro de que deseas borrar la lista "${getNombreLista(deleteListTarget!)}"?`}
        confirmText="Eliminar Lista"
        onConfirm={handleDeleteListConfirm}
        onCancel={() => setDeleteListTarget(null)}
      />

      {/* Delete Item Modal */}
      <ConfirmDialog
        isOpen={!!deleteDetailTarget}
        title="¿Remover item?"
        message="¿Deseas quitar este producto de la lista de compras?"
        confirmText="Remover"
        onConfirm={handleDeleteDetailConfirm}
        onCancel={() => setDeleteDetailTarget(null)}
      />
    </div>
  );
};
