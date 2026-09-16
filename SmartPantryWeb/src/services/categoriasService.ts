import api from './api';
import type { Categoria } from '../models/types';

export const getCategorias = async (): Promise<Categoria[]> => {
  const response = await api.get<Categoria[]>('/Categorias');
  return response.data;
};

export const getCategoria = async (id: number): Promise<Categoria> => {
  const response = await api.get<Categoria>(`/Categorias/${id}`);
  return response.data;
};
