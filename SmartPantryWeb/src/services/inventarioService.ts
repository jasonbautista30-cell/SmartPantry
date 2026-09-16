import api from './api';
import type { Inventario } from '../models/types';

export const getInventarios = async (): Promise<Inventario[]> => {
  const response = await api.get<Inventario[]>('/Inventarios');
  return response.data;
};

export const getInventario = async (id: number): Promise<Inventario> => {
  const response = await api.get<Inventario>(`/Inventarios/${id}`);
  return response.data;
};

export const createInventario = async (inventario: Omit<Inventario, 'id_inventario'>): Promise<Inventario> => {
  const response = await api.post<Inventario>('/Inventarios', inventario);
  return response.data;
};

export const updateInventario = async (id: number, inventario: Inventario): Promise<void> => {
  await api.put(`/Inventarios/${id}`, inventario);
};

export const deleteInventario = async (id: number): Promise<void> => {
  await api.delete(`/Inventarios/${id}`);
};
