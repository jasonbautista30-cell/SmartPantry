import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getCategorias } from '../services/categoriasService';
import { getProducto, updateProducto } from '../services/productosService';
import { getInventarios, updateInventario, createInventario } from '../services/inventarioService';
import type { Categoria, Inventario } from '../models/types';
import {
  getIdProducto,
  getIdCategoria,
  getIdInventario,
  getUnidadMedida,
} from '../models/types';
import { LoadingSpinner } from '../components/LoadingSpinner';

interface EditProductPageProps {
  showToast: (text: string, type?: 'success' | 'error' | 'info') => void;
}

export const EditProductPage: React.FC<EditProductPageProps> = ({ showToast }) => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [categorias, setCategorias] = useState<Categoria[]>([]);

  // Existing inventory record if found
  const [existingInventario, setExistingInventario] = useState<Inventario | null>(null);

  // Form state
  const [nombre, setNombre] = useState('');
  const [idCategoria, setIdCategoria] = useState<number>(0);
  const [descripcion, setDescripcion] = useState('');
  const [precioEstimado, setPrecioEstimado] = useState<string>('');
  
  // Inventory fields
  const [cantidad, setCantidad] = useState<number>(0);
  const [unidadMedida, setUnidadMedida] = useState<string>('unidades');
  const [fechaVencimiento, setFechaVencimiento] = useState<string>('');

  useEffect(() => {
    const loadProductData = async () => {
      if (!id) return;
      try {
        setLoading(true);
        const prodId = Number(id);

        const [prod, catRes, invRes] = await Promise.all([
          getProducto(prodId),
          getCategorias(),
          getInventarios(),
        ]);

        setCategorias(catRes);

        // Fill product fields
        setNombre(prod.nombre);
        setIdCategoria(getIdCategoria(prod));
        setDescripcion(prod.descripcion || '');
        setPrecioEstimado(prod.precio_estimado != null ? String(prod.precio_estimado) : '');

        // Find inventory record for this product
        const inv = invRes.find((i) => getIdProducto(i) === prodId);
        if (inv) {
          setExistingInventario(inv);
          setCantidad(inv.cantidad);
          setUnidadMedida(getUnidadMedida(inv));
          const fVenc = inv.fechaVencimiento ?? inv.fecha_vencimiento;
          if (fVenc) {
            setFechaVencimiento(fVenc.substring(0, 10));
          }
        }
      } catch (err) {
        console.error('Error al cargar datos para editar:', err);
        showToast('Error al cargar la información del producto', 'error');
        navigate('/inventario');
      } finally {
        setLoading(false);
      }
    };

    loadProductData();
  }, [id]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id) return;

    if (!nombre.trim()) {
      showToast('El nombre del producto es obligatorio', 'error');
      return;
    }

    try {
      setSubmitting(true);
      const prodId = Number(id);

      // 1. Update Product catalog record
      await updateProducto(prodId, {
        idProducto: prodId,
        id_producto: prodId,
        nombre: nombre.trim(),
        idCategoria: idCategoria,
        id_categoria: idCategoria,
        descripcion: descripcion.trim() || null,
        precio_estimado: precioEstimado ? parseFloat(precioEstimado) : null,
      });

      // 2. Update or create Inventory record
      if (existingInventario) {
        const invId = getIdInventario(existingInventario);
        await updateInventario(invId, {
          ...existingInventario,
          idInventario: invId,
          id_inventario: invId,
          idProducto: prodId,
          id_producto: prodId,
          cantidad: Number(cantidad),
          unidadMedida: unidadMedida,
          unidad_medida: unidadMedida,
          fechaVencimiento: fechaVencimiento || null,
          fecha_vencimiento: fechaVencimiento || null,
        });
      } else {
        await createInventario({
          idProducto: prodId,
          id_producto: prodId,
          cantidad: Number(cantidad),
          unidadMedida: unidadMedida,
          unidad_medida: unidadMedida,
          fechaVencimiento: fechaVencimiento || null,
          fecha_vencimiento: fechaVencimiento || null,
          fechaIngreso: new Date().toISOString(),
        });
      }

      showToast('Producto e inventario actualizados correctamente.', 'success');
      navigate('/inventario');
    } catch (err) {
      console.error('Error al actualizar:', err);
      showToast('Error al guardar las modificaciones', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <LoadingSpinner message="Cargando información del producto..." />;
  }

  return (
    <div className="page-container">
      <div style={{ marginBottom: '20px' }}>
        <h1 className="page-title">Editar Producto</h1>
        <p className="page-subtitle">Modificación de catálogo e inventario</p>
      </div>

      <form onSubmit={handleSubmit} className="glass-card" style={{ padding: '24px' }}>
        <div className="form-group">
          <label className="form-label">Nombre del producto *</label>
          <input
            type="text"
            className="form-input"
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
            required
          />
        </div>

        <div className="form-group">
          <label className="form-label">Categoría *</label>
          <select
            className="form-select"
            value={idCategoria}
            onChange={(e) => setIdCategoria(Number(e.target.value))}
            required
          >
            {categorias.map((c) => {
              const cId = getIdCategoria(c);
              return (
                <option key={cId} value={cId}>
                  {c.nombre}
                </option>
              );
            })}
          </select>
        </div>

        <div className="form-group">
          <label className="form-label">Descripción</label>
          <textarea
            className="form-textarea"
            rows={2}
            value={descripcion}
            onChange={(e) => setDescripcion(e.target.value)}
          />
        </div>

        <div className="form-group">
          <label className="form-label">Precio Estimado ($)</label>
          <input
            type="number"
            step="0.01"
            className="form-input"
            value={precioEstimado}
            onChange={(e) => setPrecioEstimado(e.target.value)}
          />
        </div>

        <hr style={{ border: 'none', borderTop: '1px solid rgba(255, 255, 255, 0.08)', margin: '20px 0' }} />

        <h3 style={{ fontSize: '1rem', color: '#4ade80', marginBottom: '14px' }}>📦 Datos de Inventario</h3>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
          <div className="form-group">
            <label className="form-label">Cantidad Stock *</label>
            <input
              type="number"
              min="0"
              className="form-input"
              value={cantidad}
              onChange={(e) => setCantidad(Number(e.target.value))}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Unidad de Medida *</label>
            <select
              className="form-select"
              value={unidadMedida}
              onChange={(e) => setUnidadMedida(e.target.value)}
            >
              <option value="unidades">Unidades</option>
              <option value="kg">Kilogramos (kg)</option>
              <option value="g">Gramos (g)</option>
              <option value="litros">Litros (L)</option>
              <option value="ml">Mililitros (ml)</option>
              <option value="paquetes">Paquetes</option>
              <option value="latas">Latas</option>
              <option value="cajas">Cajas</option>
            </select>
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">Fecha de Vencimiento</label>
          <input
            type="date"
            className="form-input"
            value={fechaVencimiento}
            onChange={(e) => setFechaVencimiento(e.target.value)}
          />
        </div>

        <div style={{ display: 'flex', gap: '12px', marginTop: '24px' }}>
          <button
            type="button"
            className="btn btn-secondary"
            style={{ flex: 1 }}
            onClick={() => navigate('/inventario')}
            disabled={submitting}
          >
            Cancelar
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            style={{ flex: 1 }}
            disabled={submitting}
          >
            {submitting ? 'Guardando...' : 'Actualizar'}
          </button>
        </div>
      </form>
    </div>
  );
};
