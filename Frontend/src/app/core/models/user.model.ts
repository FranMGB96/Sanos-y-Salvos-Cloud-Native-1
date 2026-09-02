export interface User {
  id: number;
  nombre: string;
  email: string;
  telefono: string;
  rol: 'OWNER' | 'CITIZEN' | 'ORG' | 'ADMIN';
  active: boolean;
  createdAt: string;
}

export interface AuthRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  nombre: string;
  email: string;
  password: string;
  telefono: string;
  rol?: string;
}

export interface AuthResponse {
  token: string;
  tipo: string;
  userId: number;
  nombre: string;
  email: string;
  telefono: string;
  rol: string;
}