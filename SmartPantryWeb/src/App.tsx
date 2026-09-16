import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Header } from './components/Header';
import { BottomNav } from './components/BottomNav';
import { ToastContainer } from './components/Toast';
import type { ToastMessage } from './components/Toast';
import { HomePage } from './pages/HomePage';
import { InventoryPage } from './pages/InventoryPage';
import { AddProductPage } from './pages/AddProductPage';
import { EditProductPage } from './pages/EditProductPage';
import { ExpiringPage } from './pages/ExpiringPage';
import { ShoppingListPage } from './pages/ShoppingListPage';
import { getInventarios } from './services/inventarioService';

export const App: React.FC = () => {
  const [toasts, setToasts] = useState<ToastMessage[]>([]);
  const [expiringCount, setExpiringCount] = useState<number>(0);

  const showToast = (text: string, type: 'success' | 'error' | 'info' = 'info') => {
    const id = Date.now().toString();
    const newToast: ToastMessage = { id, text, type };
    setToasts((prev) => [...prev, newToast]);

    // Auto dismiss after 4 seconds
    setTimeout(() => {
      removeToast(id);
    }, 4000);
  };

  const removeToast = (id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  // Check expiring count periodically for bottom nav badge
  useEffect(() => {
    const checkExpiring = async () => {
      try {
        const invs = await getInventarios();
        const now = new Date();
        const next7Days = new Date();
        next7Days.setDate(now.getDate() + 7);

        const count = invs.filter((inv) => {
          const fVenc = inv.fechaVencimiento ?? inv.fecha_vencimiento;
          if (!fVenc) return false;
          const vDate = new Date(fVenc);
          return vDate <= next7Days;
        }).length;

        setExpiringCount(count);
      } catch (err) {
        // Silent catch if API is unavailable on load
      }
    };

    checkExpiring();
  }, []);

  return (
    <Router>
      <Header />
      <ToastContainer toasts={toasts} onClose={removeToast} />

      <main style={{ flex: 1, paddingBottom: '20px' }}>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/inventario" element={<InventoryPage showToast={showToast} />} />
          <Route path="/agregar" element={<AddProductPage showToast={showToast} />} />
          <Route path="/editar/:id" element={<EditProductPage showToast={showToast} />} />
          <Route path="/por-vencer" element={<ExpiringPage showToast={showToast} />} />
          <Route path="/compras" element={<ShoppingListPage showToast={showToast} />} />
        </Routes>
      </main>

      <BottomNav expiringCount={expiringCount} />
    </Router>
  );
};

export default App;
