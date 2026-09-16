import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCategorias } from '../services/categoriasService';
import { createProducto } from '../services/productosService';
import { createInventario } from '../services/inventarioService';
import type { Categoria } from '../models/types';
import { getIdCategoria, getIdProducto } from '../models/types';
import { LoadingSpinner } from '../components/LoadingSpinner';

interface AddProductPageProps {
  showToast: (text: string, type?: 'success' | 'error' | 'info') => void;
}

export const AddProductPage: React.FC<AddProductPageProps> = ({ showToast }) => {
  const navigate = useNavigate();
  const [loadingCats, setLoadingCats] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [categorias, setCategorias] = useState<Categoria[]>([]);

  // Form State
  const [nombre, setNombre] = useState('');
  const [idCategoria, setIdCategoria] = useState<number | ''>('');
  const [descripcion, setDescripcion] = useState('');
  const [precioEstimado, setPrecioEstimado] = useState<string>('');
  
  // Inventory fields
  const [cantidad, setCantidad] = useState<number>(1);
  const [unidadMedida, setUnidadMedida] = useState<string>('unidades');
  const [fechaVencimiento, setFechaVencimiento] = useState<string>('');

  useEffect(() => {
    const fetchCats = async () => {
      try {
        const cats = await getCategorias();
        setCategorias(cats);
        if (cats.length > 0) {
          setIdCategoria(getIdCategoria(cats[0]));
        }
      } catch (err) {
        console.error('Error al cargar categorías:', err);
        showToast('Error al obtener categorías', 'error');
      } finally {
        setLoadingCats(false);
      }
    };
    fetchCats();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!nombre.trim()) {
      showToast('Por favor ingrese el nombre del producto', 'error');
      return;
    }

    if (!idCategoria) {
      showToast('Por favor seleccione una categoría', 'error');
      return;
    }

    try {
      setSubmitting(true);
      const cId = Number(idCategoria);

      // 1. Create Product catalog record in SQL Server
      const createdProd = await createProducto({
        nombre: nombre.trim(),
        idCategoria: cId,
        id_categoria: cId,
        descripcion: descripcion.trim() || null,
        precio_estimado: precioEstimado ? parseFloat(precioEstimado) : null,
      });

      const pId = getIdProducto(createdProd);

      // 2. Create associated Inventory record
      await createInventario({
        idProducto: pId,
        id_producto: pId,
        cantidad: Number(cantidad),
        unidadMedida: unidadMedida,
        unidad_medida: unidadMedida,
        fechaVencimiento: fechaVencimiento || null,
        fecha_vencimiento: fechaVencimiento || null,
        fechaIngreso: new Date().toISOString(),
      });

      showToast(`Producto "${createdProd.nombre}" registrado exitosamente.`, 'success');
      navigate('/inventario');
    } catch (err) {
      console.error('Error al registrar producto:', err);
      showToast('Error al guardar el producto en la base de datos', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  if (loadingCats) {
    return <LoadingSpinner message="Cargando formulario..." />;
  }

  return (
    <div className="page-container">
      <div style={{ marginBottom: '20px' }}>
        <h1 className="page-title">Agregar Producto</h1>
        <p className="page-subtitle">Registro de catálogo e inventario inicial</p>
      </div>

      <form onSubmit={handleSubmit} className="glass-card" style={{ padding: '24px' }}>
        <div className="form-group">
          <label className="form-label">Nombre del producto *</label>
          <input
            type="text"
            className="form-input"
            placeholder="Ej: Leche Entera 1L"
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
            <option value="" disabled>Seleccione una categoría</option>
            {categorias.map((c) => {
              const catId = getIdCategoria(c);
              return (
                <option key={catId} value={catId}>
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
            placeholder="Ej: Marca Dos Pinos, descremada"
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
            placeholder="0.00"
            value={precioEstimado}
            onChange={(e) => setPrecioEstimado(e.target.value)}
          />
        </div>

        <hr style={{ border: 'none', borderTop: '1px solid rgba(255, 255, 255, 0.08)', margin: '20px 0' }} />

        <h3 style={{ fontSize: '1rem', color: '#4ade80', marginBottom: '14px' }}>📦 Datos de Inventario</h3>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
          <div className="form-group">
            <label className="form-label">Cantidad Inicial *</label>
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
            {submitting ? 'Guardando...' : 'Guardar Producto'}
          </button>
        </div>
      </form>
    </div>
  );
};
