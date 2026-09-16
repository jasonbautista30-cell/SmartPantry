import api from './api';
import type { Producto } from '../models/types';

export const getProductos = async (): Promise<Producto[]> => {
  const response = await api.get<Producto[]>('/Productos');
  return response.data;
};

export const getProducto = async (id: number): Promise<Producto> => {
  const response = await api.get<Producto>(`/Productos/${id}`);
  return response.data;
};

export const createProducto = async (producto: Omit<Producto, 'id_producto'>): Promise<Producto> => {
  const response = await api.post<Producto>('/Productos', producto);
  return response.data;
};

export const updateProducto = async (id: number, producto: Producto): Promise<void> => {
  await api.put(`/Productos/${id}`, producto);
};

export const deleteProducto = async (id: number): Promise<void> => {
  await api.delete(`/Productos/${id}`);
};
