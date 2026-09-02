export interface Pet {
  id?: number;
  nombre: string;
  especie: string;
  raza?: string;
  color?: string;
  tamanio?: 'PEQUENIO' | 'MEDIANO' | 'GRANDE';
  fotoUrl?: string;
  descripcion?: string;
  ownerId: number;
  active?: boolean;
  createdAt?: string;
}