import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { ReportService } from '../../../core/services/report.service';
import { ReporteConDetalle } from '../../../core/models/report.model';

@Component({
  selector: 'app-report-list',
  standalone: true,
  imports: [CommonModule, RouterLink],

  template: `
    <div class="page">

      <div class="page-header">
        <div>
          <h1>Reportes</h1>
          <p>{{ reportes.length }} reporte(s)</p>
        </div>
        <a routerLink="/reports/new" class="btn-primary">+ Nuevo Reporte</a>
      </div>

      <div class="filters">
        <button [class.active]="filtro === 'TODOS'"      (click)="setFiltro('TODOS')">Todos</button>
        <button [class.active]="filtro === 'PERDIDO'"    (click)="setFiltro('PERDIDO')">Perdidos</button>
        <button [class.active]="filtro === 'ENCONTRADO'" (click)="setFiltro('ENCONTRADO')">Encontrados</button>
      </div>

      <div *ngIf="loading" class="loading">Cargando reportes...</div>

      <div class="reports-list" *ngIf="!loading">

        <div class="report-card" *ngFor="let r of reportesFiltrados">

          <div class="report-header">
            <span class="badge" [class]="r.tipo === 'PERDIDO' ? 'badge-red' : 'badge-green'">
              {{ r.tipo === 'PERDIDO' ? '🔍 PERDIDO' : '✅ ENCONTRADO' }}
            </span>
            <!-- ✅ FECHA EN EL HEADER -->
            <span class="fecha-header" *ngIf="r.createdAt">
              🕐 {{ r.createdAt | date:'dd/MM/yyyy HH:mm' }}
            </span>
            <span class="estado" [class]="'estado-' + r.estado?.toLowerCase()">
              {{ r.estado }}
            </span>
          </div>

          <div class="report-body">

            <!-- FOTO MASCOTA -->
            <img
              *ngIf="r.mascota?.fotoUrl"
              [src]="r.mascota?.fotoUrl"
              class="pet-image"
              [alt]="r.mascota?.nombre"
            >

            <p class="desc">{{ r.descripcion }}</p>

            <div class="mascota-info">
              <div class="mascota-row" *ngIf="r.mascota">
                <span class="mascota-icon">🐾</span>
                <strong>{{ r.mascota.nombre }}</strong>
                <span class="sep">·</span>
                <span>{{ r.mascota.especie }}</span>
              </div>
              <div class="dueno-row" *ngIf="r.nombreReporter || r.telefonoReporter">
                <span class="mascota-icon">👤</span>
                <span class="dueno-nombre" *ngIf="r.nombreReporter">{{ r.nombreReporter }}</span>
                <span class="sep" *ngIf="r.nombreReporter && r.telefonoReporter">·</span>
                <a *ngIf="r.telefonoReporter"
                   [href]="'tel:' + r.telefonoReporter"
                   class="dueno-tel">{{ r.telefonoReporter }}</a>
                <a *ngIf="r.telefonoReporter"
                   [href]="'https://wa.me/56' + r.telefonoReporter"
                   target="_blank"
                   class="btn-whatsapp-small">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="white">
                    <path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 01-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 01-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 012.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0012.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 005.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 00-3.48-8.413z"/>
                  </svg>
                </a>
              </div>
            </div>

            <p class="location" *ngIf="r.ubicacionDescripcion">
              📍 {{ r.ubicacionDescripcion }}
            </p>

            <!-- MAPA -->
            <div class="map-preview" *ngIf="r.latitud && r.longitud">
              <img
                [src]="getStaticMap(r.latitud, r.longitud)"
                class="report-map-image google-style-map"
                alt="Ubicación"
              >
              <div class="paw-marker">
                <span>🐾</span>
              </div>
            </div>

            <a
              *ngIf="r.latitud && r.longitud"
              [href]="'https://www.google.com/maps?q=' + r.latitud + ',' + r.longitud"
              target="_blank"
              class="map-link"
            >
              Ver ubicación completa
            </a>

          </div>

          <div class="report-actions" *ngIf="r.estado === 'ACTIVO'">
            <button (click)="resolver(r)" class="btn-resolve">
              Marcar como Resuelto
            </button>
          </div>

        </div>

      </div>

    </div>
  `,

  styles: [`
    .page{
      padding:2rem;
      max-width:950px;
      margin:0 auto;
    }

    .page-header{
      display:flex;
      justify-content:space-between;
      align-items:flex-start;
      margin-bottom:1.5rem;
    }

    .page-header h1{ font-size:1.8rem; color:#1a237e; margin:0 0 .25rem; }
    .page-header p { color:#666; margin:0; font-size:.9rem; }

    .btn-primary{
      background:#1a237e; color:white; padding:.7rem 1.4rem;
      border-radius:8px; text-decoration:none; font-weight:600; font-size:.9rem;
    }

    .filters{ display:flex; gap:.5rem; margin-bottom:1.5rem; }

    .filters button{
      padding:.45rem 1.1rem; border-radius:20px; border:2px solid #ddd;
      background:white; color:#666; cursor:pointer; font-size:.85rem; font-weight:600;
    }

    .filters button.active{ background:#1a237e; color:white; border-color:#1a237e; }

    .loading{ text-align:center; padding:3rem; color:#666; }

    .reports-list{ display:flex; flex-direction:column; gap:1rem; }

    .report-card{
      background:white; border-radius:16px; overflow:hidden;
      box-shadow:0 2px 10px rgba(0,0,0,.08);
    }

    .report-header{
      display:flex; justify-content:space-between; align-items:center;
      padding:1rem 1.25rem; background:#f8f9ff; border-bottom:1px solid #eee;
      gap:.75rem;
    }

    .badge{ padding:.35rem .9rem; border-radius:20px; font-size:.82rem; font-weight:700; }
    .badge-red  { background:#ffebee; color:#c62828; }
    .badge-green{ background:#e8f5e9; color:#2e7d32; }

    /* ✅ FECHA EN HEADER */
    .fecha-header{
      font-size:.8rem;
      color:#888;
      flex:1;
      text-align:center;
    }

    .estado{ font-size:.75rem; font-weight:700; padding:.25rem .7rem; border-radius:12px; }
    .estado-activo  { background:#fff9c4; color:#f57f17; }
    .estado-resuelto{ background:#e8f5e9; color:#2e7d32; }

    .report-body{ padding:1.25rem; }

    .pet-image{
      width:100%; height:340px;
      object-fit:contain; background:#f5f5f5;
      border-radius:14px; margin-bottom:1rem;
      padding:.5rem; display:block;
    }

    .desc{ margin:0 0 1rem; color:#333; font-size:1rem; line-height:1.5; }

    .mascota-info{
      padding:.8rem 1rem; background:#f8f9ff;
      border-radius:10px; margin-bottom:1rem;
      display:flex; flex-direction:column; gap:.5rem;
    }

    .mascota-row, .dueno-row{
      display:flex; align-items:center; gap:.4rem; flex-wrap:wrap;
    }

    .mascota-icon{ font-size:1rem; }
    .sep{ color:#ccc; }

    .mascota-row strong{ font-size:1rem; color:#1a237e; }
    .mascota-row span  { color:#666; font-size:.9rem; }

    .dueno-nombre{ font-size:.9rem; color:#333; font-weight:600; }

    .dueno-tel{
      font-size:.9rem; color:#1a237e; font-weight:700;
      text-decoration:none;
    }
    .dueno-tel:hover{ text-decoration:underline; }

    .btn-whatsapp-small{
      background:#25d366;
      border-radius:50%;
      width:32px; height:32px;
      display:flex; align-items:center; justify-content:center;
      text-decoration:none;
      flex-shrink:0;
      transition:background .2s;
    }

    .btn-whatsapp-small:hover{ background:#1da851; }

    .location{ margin:.5rem 0 1rem; font-size:.9rem; color:#666; }

    /* MAPA */
    .map-preview{
      position:relative; width:100%; margin-bottom:1rem; overflow:hidden;
      border-radius:14px; border:1px solid #d8d8d8; background:#f2f2f2;
      box-shadow:0 1px 2px rgba(0,0,0,.08), 0 3px 8px rgba(0,0,0,.12);
    }

    .report-map-image{ width:100%; height:280px; object-fit:cover; display:block; }

    .google-style-map{ filter:brightness(1.04) contrast(.96) saturate(.82); }

    .paw-marker{
      position:absolute; top:50%; left:50%;
      width:42px; height:42px;
      background:#ea4335; border-radius:50% 50% 50% 0;
      transform:translate(-50%, -100%) rotate(-45deg);
      border:3px solid white; box-shadow:0 3px 10px rgba(0,0,0,.35);
      display:flex; align-items:center; justify-content:center;
    }

    .paw-marker span{ transform:rotate(45deg); font-size:18px; line-height:1; }

    .map-link{
      display:inline-block; margin-bottom:1rem;
      color:#1a237e; font-weight:600; text-decoration:none;
    }

    .map-link:hover{ text-decoration:underline; }

    .report-actions{ padding:1rem 1.25rem; border-top:1px solid #eee; }

    .btn-resolve{
      background:#e8f5e9; color:#2e7d32; border:none;
      padding:.6rem 1.2rem; border-radius:8px;
      font-size:.9rem; font-weight:700; cursor:pointer;
    }
  `]
})

export class ReportListComponent implements OnInit {

  reportes: ReporteConDetalle[] = [];
  filtro = 'TODOS';
  loading = true;

  constructor(private reportService: ReportService) {}

  ngOnInit() {
    this.reportService.getReportesConDetalle().subscribe({
      next: r => {
        this.reportes = r.sort((a, b) =>
          new Date(b.createdAt || '').getTime() - new Date(a.createdAt || '').getTime()
        );
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  get reportesFiltrados() {
    return this.filtro === 'TODOS'
      ? this.reportes
      : this.reportes.filter(r => r.tipo === this.filtro);
  }

  setFiltro(f: string) { this.filtro = f; }

  resolver(r: ReporteConDetalle) {
    this.reportService.updateEstado(r.id!, 'RESUELTO').subscribe(() => {
      r.estado = 'RESUELTO';
    });
  }

  getStaticMap(lat: number, lng: number): string {
    return `https://maps.wikimedia.org/img/osm-intl,15,${lat},${lng},800x400.png`;
  }
}