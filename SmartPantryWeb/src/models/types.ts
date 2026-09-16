export interface Categoria {
  idCategoria?: number;
  id_categoria?: number;
  nombre: string;
  descripcion?: string | null;
}

export interface Producto {
  idProducto?: number;
  id_producto?: number;
  idCategoria?: number;
  id_categoria?: number;
  nombre: string;
  codigoBarras?: string | null;
  codigo_barras?: string | null;
  unidadMedida?: string | null;
  unidad_medida?: string | null;
  descripcion?: string | null;
  precio_estimado?: number | null;
  categoria?: Categoria | null;
}

export interface Inventario {
  idInventario?: number;
  id_inventario?: number;
  idProducto?: number;
  id_producto?: number;
  cantidad: number;
  cantidadMinima?: number;
  cantidad_minima?: number;
  unidadMedida?: string | null;
  unidad_medida?: string | null;
  fechaVencimiento?: string | null;
  fecha_vencimiento?: string | null;
  ubicacion?: string | null;
  fechaIngreso?: string | null;
  fecha_registro?: string | null;
  producto?: Producto | null;
}

export interface ListaCompra {
  idLista?: number;
  id_lista?: number;
  nombreLista?: string;
  nombre_lista?: string;
  estado?: string | null;
  fechaCreacion?: string | null;
  fecha_creacion?: string | null;
  detalles?: DetalleListaCompra[];
}

export interface DetalleListaCompra {
  idDetalle?: number;
  id_detalle?: number;
  idLista?: number;
  id_lista?: number;
  idProducto?: number;
  id_producto?: number;
  cantidadAComprar?: number;
  cantidad?: number;
  unidadMedida?: string | null;
  unidad_medida?: string | null;
  comprado?: boolean | null;
  producto?: Producto | null;
}

// Unified representation for UI displays
export interface UnifiedProduct {
  id_producto: number;
  nombre: string;
  codigo_barras?: string | null;
  descripcion?: string | null;
  precio_estimado?: number | null;
  id_categoria: number;
  nombre_categoria: string;
  
  // Inventory fields
  id_inventario?: number;
  cantidad: number;
  cantidad_minima?: number;
  unidad_medida: string;
  ubicacion?: string | null;
  fecha_vencimiento?: string | null;
  fecha_registro?: string | null;
}

// Helper normalization methods to handle camelCase / snake_case payload variations
export const getIdProducto = (p: Partial<Producto> | Partial<Inventario> | Partial<DetalleListaCompra>): number =>
  (p as any).idProducto ?? (p as any).id_producto ?? 0;

export const getIdCategoria = (c: Partial<Categoria> | Partial<Producto>): number =>
  (c as any).idCategoria ?? (c as any).id_categoria ?? 0;

export const getIdInventario = (i: Partial<Inventario>): number =>
  i.idInventario ?? i.id_inventario ?? 0;

export const getIdLista = (l: Partial<ListaCompra> | Partial<DetalleListaCompra>): number =>
  (l as any).idLista ?? (l as any).id_lista ?? 0;

export const getIdDetalle = (d: Partial<DetalleListaCompra>): number =>
  d.idDetalle ?? d.id_detalle ?? 0;

export const getNombreLista = (l: Partial<ListaCompra>): string =>
  l.nombreLista ?? l.nombre_lista ?? 'Mi Lista';

export const getCantidadDetalle = (d: Partial<DetalleListaCompra>): number =>
  d.cantidadAComprar ?? d.cantidad ?? 1;

export const getUnidadMedida = (item: { unidadMedida?: string | null; unidad_medida?: string | null; producto?: Producto | null }): string =>
  item.unidadMedida ?? item.unidad_medida ?? item.producto?.unidadMedida ?? item.producto?.unidad_medida ?? 'unidades';
