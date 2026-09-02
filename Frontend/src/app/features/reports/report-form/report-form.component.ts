import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ReportService } from '../../../core/services/report.service';
import { PetService } from '../../../core/services/pet.service';
import { AuthService } from '../../../core/services/auth.service';
import { Pet } from '../../../core/models/pet.model';

@Component({
  selector: 'app-report-form',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <a routerLink="/reports" class="back-link">← Volver</a>
        <h1>Nuevo Reporte</h1>
      </div>

      <div class="form-card">
        <form [formGroup]="form" (ngSubmit)="onSubmit()">

          <div class="field">
            <label>Tipo de reporte *</label>
            <div class="tipo-selector">
              <label class="tipo-option" [class.selected]="f('tipo').value === 'PERDIDO'">
                <input type="radio" formControlName="tipo" value="PERDIDO">
                <span>🔍 Mascota Perdida</span>
              </label>
              <label class="tipo-option" [class.selected]="f('tipo').value === 'ENCONTRADO'">
                <input type="radio" formControlName="tipo" value="ENCONTRADO">
                <span>✅ Mascota Encontrada</span>
              </label>
            </div>
          </div>

          <div class="field">
            <label>Descripción *</label>
            <textarea formControlName="descripcion" rows="3"
                      placeholder="Describe la situación con el mayor detalle posible..."
                      [class.error]="f('descripcion').invalid && f('descripcion').touched"></textarea>
            <span class="err" *ngIf="f('descripcion').invalid && f('descripcion').touched">Obligatorio</span>
          </div>

          <div class="field">
            <label>Mascota asociada *</label>
            <select formControlName="petId" [class.error]="f('petId').invalid && f('petId').touched">
              <option value="">Selecciona una mascota...</option>
              <option *ngFor="let p of mismascotas" [value]="p.id">
                {{ p.nombre }} ({{ p.especie }})
              </option>
            </select>
            <span class="err" *ngIf="f('petId').invalid && f('petId').touched">Obligatorio</span>
          </div>

          <div class="field">
            <div class="map-header">
              <label>📍 Ubicación en el mapa</label>
              <button type="button" class="btn-ubicacion" (click)="obtenerMiUbicacion()" [disabled]="buscandoUbicacion">
                {{ buscandoUbicacion ? '⏳ Buscando...' : '📍 Usar mi ubicación actual' }}
              </button>
            </div>
            
            <div class="map-hint">
              <span *ngIf="!ubicacionSeleccionada">🖱️ Haz clic en el mapa para marcar dónde ocurrió</span>
              <span *ngIf="ubicacionSeleccionada">✅ Ubicación marcada — haz clic en otro punto para cambiarla</span>
            </div>
            <div id="mapa-reporte" class="mapa-container"></div>
          </div>

          <div class="field">
            <label>Descripción del lugar *</label>
            <input type="text" formControlName="ubicacionDescripcion"
                   placeholder="Ej: Parque O'Higgins, entrada principal"
                   [class.error]="f('ubicacionDescripcion').invalid && f('ubicacionDescripcion').touched">
            <span class="err" *ngIf="f('ubicacionDescripcion').invalid && f('ubicacionDescripcion').touched">Obligatorio</span>
          </div>

          <div class="alert-success" *ngIf="successMsg">{{ successMsg }}</div>
          <div class="alert-error"   *ngIf="errorMsg">{{ errorMsg }}</div>

          <div class="form-actions">
            <a routerLink="/reports" class="btn-cancel">Cancelar</a>
            <button type="submit" [disabled]="loading" class="btn-submit">
              {{ loading ? 'Guardando...' : 'Publicar Reporte' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .page { padding: 2rem; max-width: 700px; margin: 0 auto; }
    .page-header { margin-bottom: 1.5rem; }
    .back-link { color: #1a237e; text-decoration: none; font-size: .9rem; display: block; margin-bottom: .5rem; }
    h1 { font-size: 1.6rem; color: #1a237e; margin: 0; }
    .form-card { background: white; border-radius: 12px; padding: 2rem; box-shadow: 0 2px 10px rgba(0,0,0,.08); }
    .field { margin-bottom: 1.4rem; }
    label { display: block; font-size: .85rem; font-weight: 600; color: #333; margin-bottom: .4rem; }
    input, select, textarea {
      width: 100%; padding: .7rem 1rem; border: 1.5px solid #ddd;
      border-radius: 8px; font-size: .9rem; box-sizing: border-box;
      outline: none; transition: border .2s; font-family: inherit; background: white;
    }
    input:focus, select:focus, textarea:focus { border-color: #1a237e; }
    input.error, select.error, textarea.error { border-color: #e53935; }
    .err { color: #e53935; font-size: .78rem; margin-top: .2rem; display: block; }
    .tipo-selector { display: flex; gap: 1rem; }
    .tipo-option {
      flex: 1; display: flex; align-items: center; justify-content: center;
      gap: .5rem; padding: .75rem; border: 2px solid #ddd; border-radius: 10px;
      cursor: pointer; font-size: .9rem; font-weight: 600; color: #555; transition: all .2s;
    }
    .tipo-option input { display: none; }
    .tipo-option.selected { border-color: #1a237e; background: #e8eaf6; color: #1a237e; }
    
    .map-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: .4rem; }
    .map-header label { margin-bottom: 0; }
    .btn-ubicacion { 
      background: #e3f2fd; color: #1565c0; border: 1px solid #90caf9; 
      padding: .4rem .8rem; border-radius: 6px; font-size: .8rem; font-weight: 600;
      cursor: pointer; transition: all .2s; margin: 0;
    }
    .btn-ubicacion:hover { background: #bbdefb; }
    .btn-ubicacion:disabled { opacity: 0.7; cursor: wait; }
    
    .map-hint {
      font-size: .82rem; color: #555; margin-bottom: .6rem;
      padding: .5rem .75rem; background: #f0f4ff; border-radius: 6px;
      border-left: 3px solid #1a237e;
    }
    .mapa-container {
      width: 100%; height: 380px; border-radius: 10px;
      border: 2px solid #ddd; overflow: hidden;
      box-shadow: 0 2px 8px rgba(0,0,0,.1);
    }
    .alert-success { background: #e8f5e9; color: #2e7d32; padding: .75rem 1rem; border-radius: 8px; margin-bottom: 1rem; font-size: .85rem; }
    .alert-error   { background: #ffebee; color: #c62828; padding: .75rem 1rem; border-radius: 8px; margin-bottom: 1rem; font-size: .85rem; }
    .form-actions { display: flex; gap: 1rem; justify-content: flex-end; margin-top: 1.5rem; }
    .btn-cancel { padding: .7rem 1.5rem; border-radius: 8px; border: 2px solid #ddd; color: #666; text-decoration: none; font-weight: 600; font-size: .9rem; }
    .btn-submit { padding: .7rem 2rem; background: #1a237e; color: white; border: none; border-radius: 8px; font-size: .9rem; font-weight: 600; cursor: pointer; }
    .btn-submit:disabled { opacity: .6; cursor: not-allowed; }
  `]
})
export class ReportFormComponent implements OnInit, AfterViewInit, OnDestroy {
  form: FormGroup;
  mismascotas: Pet[] = [];
  loading = false;
  successMsg = '';
  errorMsg = '';
  ubicacionSeleccionada = false;
  buscandoUbicacion = false;

  private mapa: any = null;
  private marcador: any = null;

  constructor(
    private fb: FormBuilder,
    private reportService: ReportService,
    private petService: PetService,
    private auth: AuthService,
    private router: Router,
    private http: HttpClient
  ) {
    this.form = this.fb.group({
      tipo:                 ['PERDIDO', Validators.required],
      descripcion:          ['', Validators.required],
      petId:                ['', Validators.required], // SE AGREGÓ Validators.required
      ubicacionDescripcion: ['', Validators.required],
      latitud:              [null],
      longitud:             [null]
    });
  }

  ngOnInit() {
    const user = this.auth.getCurrentUser();
    if (user) {
      this.petService.getByOwner(user.userId).subscribe({
        next: p => this.mismascotas = p,
        error: () => {}
      });
    }
    this.cargarLeaflet();
  }

  ngAfterViewInit() {}

  ngOnDestroy() {
    if (this.mapa) { this.mapa.remove(); this.mapa = null; }
  }

  f(n: string) { return this.form.get(n)!; }

  private cargarLeaflet() {
    if (!document.getElementById('leaflet-css')) {
      const link = document.createElement('link');
      link.id = 'leaflet-css';
      link.rel = 'stylesheet';
      link.href = 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.css';
      document.head.appendChild(link);
    }

    if ((window as any).L) {
      setTimeout(() => this.inicializarMapa(), 200);
      return;
    }

    const script = document.createElement('script');
    script.src = 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.js';
    script.onload = () => setTimeout(() => this.inicializarMapa(), 200);
    document.head.appendChild(script);
  }

  private inicializarMapa() {
    const L = (window as any).L;
    const contenedor = document.getElementById('mapa-reporte');
    if (!contenedor || !L) return;

    const latDefault = -33.4489;
    const lngDefault = -70.6693;

    this.mapa = L.map('mapa-reporte').setView([latDefault, lngDefault], 13);

    L.tileLayer('http://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
      maxZoom: 20,
      subdomains: ['mt0', 'mt1', 'mt2', 'mt3'],
      attribution: 'Map data © Google'
    }).addTo(this.mapa);

    this.mapa.on('click', (e: any) => {
      this.colocarMarcador(L, e.latlng.lat, e.latlng.lng);
    });
  }

  obtenerMiUbicacion() {
    if (!navigator.geolocation) {
      alert('Tu navegador no soporta la geolocalización.');
      return;
    }

    this.buscandoUbicacion = true;
    const opciones = { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 };

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const lat = pos.coords.latitude;
        const lng = pos.coords.longitude;
        const L = (window as any).L;
        
        this.mapa.setView([lat, lng], 16);
        this.colocarMarcador(L, lat, lng);
        this.buscandoUbicacion = false;
      },
      (error) => {
        console.error('Error obteniendo ubicación', error);
        alert('No pudimos acceder a tu ubicación. Verifica si le diste permisos al navegador.');
        this.buscandoUbicacion = false;
      }
    );
  }

  private colocarMarcador(L: any, lat: number, lng: number) {
    if (this.marcador) this.mapa.removeLayer(this.marcador);

    const icono = L.divIcon({
      html: `<div style="
        background:#e53935; width:36px; height:36px; border-radius:50% 50% 50% 0;
        transform:rotate(-45deg); border:3px solid white;
        box-shadow:0 3px 10px rgba(0,0,0,.4);
        display:flex; align-items:center; justify-content:center;">
        <span style="transform:rotate(45deg); font-size:15px; line-height:1;">🐾</span>
      </div>`,
      className: '',
      iconSize: [36, 36],
      iconAnchor: [18, 36],
      popupAnchor: [0, -38]
    });

    this.marcador = L.marker([lat, lng], { icon: icono })
      .addTo(this.mapa)
      .bindPopup(`<div style="text-align:center; font-size:.85rem;">
        <b>📍 Ubicación marcada</b><br>
        <small>${lat.toFixed(5)}, ${lng.toFixed(5)}</small>
      </div>`)
      .openPopup();

    this.form.patchValue({ latitud: lat, longitud: lng });
    this.ubicacionSeleccionada = true;

    this.obtenerDireccion(lat, lng);
  }

  private obtenerDireccion(lat: number, lng: number) {
    this.form.patchValue({ ubicacionDescripcion: 'Obteniendo dirección...' });

    const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`;
    
    this.http.get(url).subscribe({
      next: (response: any) => {
        if (response && response.display_name) {
          this.form.patchValue({ ubicacionDescripcion: response.display_name });
        } else {
          this.form.patchValue({ ubicacionDescripcion: '' });
        }
      },
      error: (err) => {
        console.error('Error al obtener la dirección:', err);
        this.form.patchValue({ ubicacionDescripcion: '' });
      }
    });
  }

  onSubmit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.errorMsg = '';
    const user = this.auth.getCurrentUser();
    const raw = this.form.value;
    this.reportService.create({ ...raw, petId: raw.petId || null, reporterUserId: user?.userId }).subscribe({
      next: () => { this.successMsg = 'Reporte publicado con éxito'; setTimeout(() => this.router.navigate(['/reports']), 1200); },
      error: () => { this.errorMsg = 'Error al publicar.'; this.loading = false; }
    });
  }
}