export type ReportTipo   = 'PERDIDO' | 'ENCONTRADO';
export type ReportEstado = 'ACTIVO'  | 'RESUELTO' | 'CERRADO';

export interface Report {
  id?: number;
  tipo: ReportTipo;
  descripcion: string;
  latitud?: number;
  longitud?: number;
  ubicacionDescripcion?: string;
  petId?: number;
  reporterUserId: number;
  estado?: ReportEstado;
  createdAt?: string;
  updatedAt?: string;
}

export interface ReporteConDetalle extends Report {
  mascota?: {
    id: number;
    nombre: string;
    especie: string;
    raza?: string;
    color?: string;
    fotoUrl?: string;
  };
  // ✅ Teléfono del dueño para poder contactarlo
  telefonoReporter?: string;
  nombreReporter?: string;
}

export interface Dashboard {
  totalUsuarios: number;
  totalMascotas: number;
  totalReportes: number;
  reportesActivos: number;
  reportesPerdidos: number;
  reportesEncontrados: number;
  ultimosReportes: ReporteConDetalle[];
}