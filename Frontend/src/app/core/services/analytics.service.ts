import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AlertaZona, EstadoStreams, ResumenReportes } from '../models/analytics.model';
import { SIN_TOAST_DE_ERROR } from '../interceptors/error.interceptor';

/** Consume analytics-service a través del api-gateway (/api/analytics/**), con el token de Azure. */
@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private url = `${environment.apiUrl}/analytics`;
  // El panel maneja sus propios errores (ver ErrorInterceptor)
  private opciones = { context: new HttpContext().set(SIN_TOAST_DE_ERROR, true) };

  constructor(private http: HttpClient) {}

  getResumen(): Observable<ResumenReportes> {
    return this.http.get<ResumenReportes>(`${this.url}/resumen`, this.opciones);
  }

  getAlertas(): Observable<AlertaZona[]> {
    return this.http.get<AlertaZona[]>(`${this.url}/alertas`, this.opciones);
  }

  getEstado(): Observable<EstadoStreams> {
    return this.http.get<EstadoStreams>(`${this.url}/estado`, this.opciones);
  }
}
