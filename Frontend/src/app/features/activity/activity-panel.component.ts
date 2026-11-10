import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Subscription, forkJoin, of, timer } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';

import { AnalyticsService } from '../../core/services/analytics.service';
import { AlertaZona, ResumenReportes } from '../../core/models/analytics.model';

/**
 * Panel de actividad en tiempo real (Evaluación 3).
 * Muestra lo que calcula analytics-service con Kafka Streams a partir de los eventos
 * del topic sys.reports.events. Se actualiza solo cada 5 segundos.
 */
@Component({
  selector: 'app-activity-panel',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe],
  template: `
    <div class="page">
      <div class="header">
        <div>
          <h1>📊 Panel de actividad</h1>
          <p class="subtitle">
            Datos procesados en tiempo real con Kafka Streams · se actualiza cada 5 segundos
          </p>
        </div>
        <span class="estado" [class.ok]="estado === 'RUNNING'" [class.mal]="error">
          {{ error ? 'Sin conexión' : (estado === 'RUNNING' ? '● En vivo' : estado) }}
        </span>
      </div>

      <div *ngIf="error" class="aviso">
        No se pudo conectar con el servicio de analytics. Se vuelve a intentar automáticamente.
      </div>

      <section *ngIf="resumen">
        <h2>Reportes desde que se activó Kafka</h2>
        <div class="stats-grid">
          <div class="stat-card blue">
            <span class="stat-value">{{ resumen.totalReportes }}</span>
            <span class="stat-label">Reportes nuevos</span>
          </div>
          <div class="stat-card red">
            <span class="stat-value">{{ resumen.perdidos }}</span>
            <span class="stat-label">Perdidos</span>
          </div>
          <div class="stat-card green">
            <span class="stat-value">{{ resumen.encontrados }}</span>
            <span class="stat-label">Encontrados</span>
          </div>
          <div class="stat-card orange">
            <span class="stat-value">{{ resumen.activos }}</span>
            <span class="stat-label">Activos</span>
          </div>
          <div class="stat-card teal">
            <span class="stat-value">{{ resumen.resueltos }}</span>
            <span class="stat-label">Resueltos</span>
          </div>
          <div class="stat-card gray">
            <span class="stat-value">{{ resumen.cerrados }}</span>
            <span class="stat-label">Cerrados</span>
          </div>
        </div>
        <p class="meta">
          Eventos procesados: <strong>{{ resumen.eventosProcesados }}</strong>
          <span *ngIf="resumen.ultimaActualizacion">
            · último evento: {{ resumen.ultimaActualizacion | date:'dd-MM-yyyy HH:mm:ss' }}
          </span>
        </p>
      </section>

      <section>
        <h2>🚨 Alertas por zona</h2>
        <p class="ayuda">
          Se genera una alerta cuando hay 3 o más mascotas perdidas en la misma zona (~1 km) en menos de 1 hora.
        </p>

        <div *ngIf="alertas.length === 0 && !error" class="vacio">
          No hay alertas por ahora. <a routerLink="/reports/new">Crear un reporte</a>
        </div>

        <div *ngFor="let a of alertas" class="alerta" [class.alta]="a.severidad === 'ALTA'">
          <div class="alerta-top">
            <span class="sev">{{ a.severidad }}</span>
            <span class="hora">{{ a.timestamp | date:'dd-MM-yyyy HH:mm:ss' }}</span>
          </div>
          <p class="motivo">{{ a.motivo }}</p>
          <p class="detalle">
            Zona {{ a.zona }} · reportes #{{ a.reportIds.join(', #') }}
          </p>
        </div>
      </section>
    </div>
  `,
  styles: [`
    .page{max-width:1100px;margin:0 auto;padding:1.5rem 1rem 3rem}
    .header{display:flex;justify-content:space-between;align-items:flex-start;gap:1rem;margin-bottom:1rem}
    h1{margin:0;color:#1a237e}
    h2{margin:1.5rem 0 .75rem;color:#1a237e;font-size:1.2rem}
    .subtitle,.ayuda,.meta{color:#555;margin:.25rem 0}
    .estado{padding:.35rem .8rem;border-radius:20px;background:#eee;color:#555;font-weight:600;white-space:nowrap}
    .estado.ok{background:#e8f5e9;color:#2e7d32}
    .estado.mal{background:#ffebee;color:#c62828}
    .aviso{background:#fff3e0;border:1px solid #ffb74d;color:#e65100;padding:.75rem 1rem;border-radius:8px}
    .stats-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:1rem}
    .stat-card{border-radius:12px;padding:1.2rem;color:white;display:flex;flex-direction:column;gap:.3rem}
    .stat-card.blue{background:linear-gradient(135deg,#1a237e,#3949ab)}
    .stat-card.red{background:linear-gradient(135deg,#b71c1c,#e53935)}
    .stat-card.green{background:linear-gradient(135deg,#2e7d32,#43a047)}
    .stat-card.orange{background:linear-gradient(135deg,#e65100,#fb8c00)}
    .stat-card.teal{background:linear-gradient(135deg,#00695c,#00897b)}
    .stat-card.gray{background:linear-gradient(135deg,#455a64,#78909c)}
    .stat-value{font-size:2rem;font-weight:700;line-height:1}
    .stat-label{font-size:.9rem;opacity:.9}
    .vacio{background:#f5f5f5;border-radius:8px;padding:1rem;color:#555}
    .alerta{border-left:6px solid #fb8c00;background:#fff8e1;border-radius:8px;padding:.8rem 1rem;margin-bottom:.75rem}
    .alerta.alta{border-left-color:#e53935;background:#ffebee}
    .alerta-top{display:flex;justify-content:space-between;align-items:center}
    .sev{font-weight:700;font-size:.8rem;padding:.15rem .6rem;border-radius:10px;background:#fb8c00;color:white}
    .alerta.alta .sev{background:#e53935}
    .hora{color:#777;font-size:.85rem}
    .motivo{margin:.5rem 0 .25rem;font-weight:600}
    .detalle{margin:0;color:#555;font-size:.9rem}
  `]
})
export class ActivityPanelComponent implements OnInit, OnDestroy {
  resumen?: ResumenReportes;
  alertas: AlertaZona[] = [];
  estado = 'Conectando...';
  error = false;

  private sub?: Subscription;

  constructor(private analytics: AnalyticsService) {}

  ngOnInit(): void {
    // Consulta inmediata y luego cada 5 segundos
    this.sub = timer(0, 5000).pipe(
      switchMap(() => forkJoin({
        estado: this.analytics.getEstado(),
        resumen: this.analytics.getResumen(),
        alertas: this.analytics.getAlertas(),
      }).pipe(catchError(() => of(null))))
    ).subscribe(datos => {
      if (!datos) {
        this.error = true;
        return;
      }
      this.error = false;
      this.estado = datos.estado.estado;
      this.resumen = datos.resumen;
      this.alertas = datos.alertas;
    });
  }

  ngOnDestroy(): void {
    // Al salir de la página se deja de consultar
    this.sub?.unsubscribe();
  }
}
