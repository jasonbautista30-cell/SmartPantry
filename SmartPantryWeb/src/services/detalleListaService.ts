import api from './api';
import type { DetalleListaCompra } from '../models/types';

export const getDetalles = async (): Promise<DetalleListaCompra[]> => {
  const response = await api.get<DetalleListaCompra[]>('/DetalleListaCompras');
  return response.data;
};

export const getDetalle = async (id: number): Promise<DetalleListaCompra> => {
  const response = await api.get<DetalleListaCompra>(`/DetalleListaCompras/${id}`);
  return response.data;
};

export const createDetalle = async (detalle: Omit<DetalleListaCompra, 'id_detalle'>): Promise<DetalleListaCompra> => {
  const response = await api.post<DetalleListaCompra>('/DetalleListaCompras', detalle);
  return response.data;
};

export const updateDetalle = async (id: number, detalle: DetalleListaCompra): Promise<void> => {
  await api.put(`/DetalleListaCompras/${id}`, detalle);
};

export const deleteDetalle = async (id: number): Promise<void> => {
  await api.delete(`/DetalleListaCompras/${id}`);
};
