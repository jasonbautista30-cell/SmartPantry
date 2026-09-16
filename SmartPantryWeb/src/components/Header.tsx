import React from 'react';

export const Header: React.FC = () => {
  return (
    <header className="app-header">
      <div className="brand">
        <div className="brand-icon">
          🥗
        </div>
        <div>
          <div className="brand-title">Smart Pantry</div>
          <div className="brand-sub">ESFE-AGAPE • NEXO DEVS</div>
        </div>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
        <span className="badge badge-success">Online</span>
      </div>
    </header>
  );
};
