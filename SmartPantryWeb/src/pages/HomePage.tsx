import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getProductos } from '../services/productosService';
import { getInventarios } from '../services/inventarioService';
import { getCategorias } from '../services/categoriasService';
import type { Producto, Inventario, Categoria } from '../models/types';
import { LoadingSpinner } from '../components/LoadingSpinner';

export const HomePage: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [productos, setProductos] = useState<Producto[]>([]);
  const [inventarios, setInventarios] = useState<Inventario[]>([]);
  const [categorias, setCategorias] = useState<Categoria[]>([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [prodRes, invRes, catRes] = await Promise.all([
          getProductos(),
          getInventarios(),
          getCategorias(),
        ]);
        setProductos(prodRes);
        setInventarios(invRes);
        setCategorias(catRes);
      } catch (err) {
        console.error('Error al cargar datos del dashboard:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  // Calculate statistics
  const totalProductos = productos.length;
  
  const now = new Date();
  const next7Days = new Date();
  next7Days.setDate(now.getDate() + 7);

  const porVencerCount = inventarios.filter((inv) => {
    const fVenc = inv.fechaVencimiento ?? inv.fecha_vencimiento;
    if (!fVenc) return false;
    const vDate = new Date(fVenc);
    return vDate <= next7Days;
  }).length;

  const sinStockCount = inventarios.filter((inv) => inv.cantidad <= 0).length;

  if (loading) {
    return <LoadingSpinner message="Cargando resumen de la despensa..." />;
  }

  return (
    <div className="page-container">
      <div style={{ marginBottom: '24px' }}>
        <h1 className="page-title">Mi Despensa</h1>
        <p className="page-subtitle">Gestión inteligente de alimentos e inventario</p>
      </div>

      {/* Grid of Stats */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px', marginBottom: '24px' }}>
        <div className="glass-card" style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: '6px' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Total Productos</span>
          <span style={{ fontSize: '1.8rem', fontWeight: '700', color: '#4ade80' }}>{totalProductos}</span>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>En catálogo</span>
        </div>

        <div className="glass-card" style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: '6px' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Por Vencer / Vencidos</span>
          <span style={{ fontSize: '1.8rem', fontWeight: '700', color: porVencerCount > 0 ? '#f97316' : '#f8fafc' }}>
            {porVencerCount}
          </span>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Próximos 7 días</span>
        </div>

        <div className="glass-card" style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: '6px' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Categorías</span>
          <span style={{ fontSize: '1.8rem', fontWeight: '700', color: '#60a5fa' }}>{categorias.length}</span>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Registradas</span>
        </div>

        <div className="glass-card" style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: '6px' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Sin Stock</span>
          <span style={{ fontSize: '1.8rem', fontWeight: '700', color: sinStockCount > 0 ? '#ef4444' : '#f8fafc' }}>
            {sinStockCount}
          </span>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Requieren compra</span>
        </div>
      </div>

      {/* Direct Quick Actions */}
      <h2 style={{ fontSize: '1.1rem', fontWeight: '600', marginBottom: '14px' }}>Acciones Rápidas</h2>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '28px' }}>
        <Link to="/agregar" className="btn btn-primary" style={{ justifyContent: 'flex-start', padding: '14px 18px' }}>
          <span style={{ fontSize: '1.2rem' }}>➕</span>
          <span>Registrar Nuevo Producto</span>
        </Link>
        <Link to="/inventario" className="btn btn-secondary" style={{ justifyContent: 'flex-start', padding: '14px 18px' }}>
          <span style={{ fontSize: '1.2rem' }}>📦</span>
          <span>Ver Inventario Completo</span>
        </Link>
        <Link to="/compras" className="btn btn-secondary" style={{ justifyContent: 'flex-start', padding: '14px 18px' }}>
          <span style={{ fontSize: '1.2rem' }}>🛒</span>
          <span>Gestión de Lista de Compras</span>
        </Link>
      </div>

      {/* Urgent Warning Section if items are expiring */}
      {porVencerCount > 0 && (
        <div className="glass-card" style={{ borderLeft: '4px solid var(--accent)', background: 'rgba(249, 115, 22, 0.1)' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div>
              <h3 style={{ fontSize: '1rem', color: '#fdba74', marginBottom: '4px' }}>⚠️ Alerta de Vencimiento</h3>
              <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                Tienes {porVencerCount} producto(s) por vencer pronto.
              </p>
            </div>
            <Link to="/por-vencer" className="btn btn-secondary" style={{ fontSize: '0.8rem', padding: '8px 12px' }}>
              Revisar
            </Link>
          </div>
        </div>
      )}
    </div>
  );
};
