/** Contadores en vivo que calcula analytics-service con Kafka Streams. */
export interface ResumenReportes {
  totalReportes: number;
  perdidos: number;
  encontrados: number;
  activos: number;
  resueltos: number;
  cerrados: number;
  eventosProcesados: number;
  ultimaActualizacion?: string;
}

/** Alerta por zona publicada en el topic sys.reports.alerts. */
export interface AlertaZona {
  alertaId: string;
  zona: string;
  tipo: string;
  cantidad: number;
  ventanaMinutos: number;
  reportIds: number[];
  severidad: 'MEDIA' | 'ALTA';
  motivo: string;
  timestamp: string;
}

export interface EstadoStreams {
  estado: string;
}
