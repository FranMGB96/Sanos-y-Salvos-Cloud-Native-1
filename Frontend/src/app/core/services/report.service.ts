import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  Dashboard,
  Report,
  ReporteConDetalle,
} from '../models/report.model';

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  private reportUrl = `${environment.apiUrl}/reports`;
  private bffUrl = `${environment.apiUrl}/bff`;

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<Dashboard> {
    return this.http.get<Dashboard>(`${this.bffUrl}/dashboard`);
  }

  getReportesConDetalle(): Observable<ReporteConDetalle[]> {
    return this.http.get<ReporteConDetalle[]>(`${this.bffUrl}/reportes`);
  }

  getReportesPorTipo(tipo: string): Observable<ReporteConDetalle[]> {
    return this.http.get<ReporteConDetalle[]>(
      `${this.bffUrl}/reportes/tipo/${tipo}`
    );
  }

  getAll(): Observable<Report[]> {
    return this.http.get<Report[]>(this.reportUrl);
  }

  getById(id: number): Observable<Report> {
    return this.http.get<Report>(`${this.reportUrl}/${id}`);
  }

  create(report: Report): Observable<Report> {
    return this.http.post<Report>(this.reportUrl, report);
  }

  update(id: number, report: Partial<Report>): Observable<Report> {
    return this.http.put<Report>(`${this.reportUrl}/${id}`, report);
  }

  updateEstado(id: number, estado: string): Observable<Report> {
    return this.http.patch<Report>(
      `${this.reportUrl}/${id}/estado?estado=${estado}`,
      {}
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.reportUrl}/${id}`);
  }
}