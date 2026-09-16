import api from './api';
import type { ListaCompra } from '../models/types';

export const getListasCompras = async (): Promise<ListaCompra[]> => {
  const response = await api.get<ListaCompra[]>('/ListasCompras');
  return response.data;
};

export const getListaCompra = async (id: number): Promise<ListaCompra> => {
  const response = await api.get<ListaCompra>(`/ListasCompras/${id}`);
  return response.data;
};

export const createListaCompra = async (lista: Omit<ListaCompra, 'id_lista'>): Promise<ListaCompra> => {
  const response = await api.post<ListaCompra>('/ListasCompras', lista);
  return response.data;
};

export const updateListaCompra = async (id: number, lista: ListaCompra): Promise<void> => {
  await api.put(`/ListasCompras/${id}`, lista);
};

export const deleteListaCompra = async (id: number): Promise<void> => {
  await api.delete(`/ListasCompras/${id}`);
};
