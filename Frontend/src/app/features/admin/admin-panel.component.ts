import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { PetService } from '../../core/services/pet.service';
import { ReportService } from '../../core/services/report.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="page">

      <div class="admin-header">
        <div>
          <h1>⚙️ Panel de Administración</h1>
          <p>Bienvenido, {{ currentUser?.nombre }} — <span class="badge-admin">ADMIN</span></p>
        </div>
      </div>

      <!-- Stats -->
      <div class="stats-grid">
        <div class="stat-card">
          <span class="stat-icon">👥</span>
          <span class="stat-number">{{ usuarios.length }}</span>
          <span class="stat-label">Usuarios</span>
        </div>
        <div class="stat-card">
          <span class="stat-icon">🐾</span>
          <span class="stat-number">{{ mascotas.length }}</span>
          <span class="stat-label">Mascotas</span>
        </div>
        <div class="stat-card">
          <span class="stat-icon">📋</span>
          <span class="stat-number">{{ reportes.length }}</span>
          <span class="stat-label">Reportes</span>
        </div>
        <div class="stat-card">
          <span class="stat-icon">🔴</span>
          <span class="stat-number">{{ reportesActivos }}</span>
          <span class="stat-label">Reportes Activos</span>
        </div>
      </div>

      <!-- Tabs -->
      <div class="tabs">
        <button [class.active]="tab === 'usuarios'" (click)="tab='usuarios'">👥 Usuarios</button>
        <button [class.active]="tab === 'mascotas'" (click)="tab='mascotas'">🐾 Mascotas</button>
        <button [class.active]="tab === 'reportes'" (click)="tab='reportes'">📋 Reportes</button>
      </div>

      <!-- ═══ USUARIOS ═══ -->
      <div *ngIf="tab === 'usuarios'" class="table-card">
        <div *ngIf="loadingUsuarios" class="loading">Cargando usuarios...</div>
        <table *ngIf="!loadingUsuarios">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre</th>
              <th>Email</th>
              <th>Teléfono</th>
              <th>Rol</th>
              <th>Estado</th>
              <th>Registro</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <ng-container *ngFor="let u of usuarios">
              <tr *ngIf="editandoUsuarioId !== u.id">
                <td>{{ u.id }}</td>
                <td>{{ u.nombre }}</td>
                <td>{{ u.email }}</td>
                <td>{{ u.telefono || '—' }}</td>
                <td><span class="badge" [class.badge-admin]="u.rol==='ADMIN'">{{ u.rol }}</span></td>
                <td>
                  <span class="badge" [class.badge-active]="u.active" [class.badge-inactive]="!u.active">
                    {{ u.active ? 'Activo' : 'Inactivo' }}
                  </span>
                </td>
                <td>{{ u.createdAt | date:'dd/MM/yyyy' }}</td>
                <td class="actions-cell">
                  <button class="btn-edit" (click)="iniciarEdicionUsuario(u)">✏️ Editar</button>
                  <button
                    class="btn-toggle"
                    [class.btn-deactivate]="u.active"
                    [class.btn-activate]="!u.active"
                    (click)="toggleActivo(u)"
                    [disabled]="u.id === currentUser?.userId"
                  >
                    {{ u.active ? '🔒 Desactivar' : '🔓 Activar' }}
                  </button>
                </td>
              </tr>

              <tr *ngIf="editandoUsuarioId === u.id" class="fila-editando">
                <td>{{ u.id }}</td>
                <td><input class="input-edit" [(ngModel)]="edicionNombre" placeholder="Nombre" /></td>
                <td><input class="input-edit" [(ngModel)]="edicionEmail" placeholder="Email" type="email" /></td>
                <td><input class="input-edit" [(ngModel)]="edicionTelefono" placeholder="Teléfono" type="tel" /></td>
                <td>
                  <select class="select-edit" [(ngModel)]="edicionRol" [disabled]="u.id === currentUser?.userId">
                    <option value="OWNER">OWNER</option>
                    <option value="CITIZEN">CITIZEN</option>
                    <option value="ORG">ORG</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </td>
                <td>
                  <span class="badge" [class.badge-active]="u.active" [class.badge-inactive]="!u.active">
                    {{ u.active ? 'Activo' : 'Inactivo' }}
                  </span>
                </td>
                <td>{{ u.createdAt | date:'dd/MM/yyyy' }}</td>
                <td class="actions-cell">
                  <button class="btn-save" (click)="guardarEdicionUsuario(u)" [disabled]="guardando">
                    {{ guardando ? '...' : '💾 Guardar' }}
                  </button>
                  <button class="btn-cancel-edit" (click)="cancelarEdicion()">✖ Cancelar</button>
                </td>
              </tr>
            </ng-container>
          </tbody>
        </table>
      </div>

      <!-- ═══ MASCOTAS ═══ -->
      <div *ngIf="tab === 'mascotas'" class="table-card">
        <div *ngIf="loadingMascotas" class="loading">Cargando mascotas...</div>
        <table *ngIf="!loadingMascotas">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre</th>
              <th>Especie</th>
              <th>Raza</th>
              <th>Color</th>
              <th>Tamaño</th>
              <th>Dueño ID</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <ng-container *ngFor="let p of mascotas">

              <!-- Fila normal -->
              <tr *ngIf="editandoMascotaId !== p.id">
                <td>{{ p.id }}</td>
                <td>{{ p.nombre }}</td>
                <td>{{ p.especie }}</td>
                <td>{{ p.raza || '—' }}</td>
                <td>{{ p.color || '—' }}</td>
                <td>{{ p.tamanio || '—' }}</td>
                <td>{{ p.ownerId }}</td>
                <td>
                  <span class="badge" [class.badge-active]="p.active" [class.badge-inactive]="!p.active">
                    {{ p.active ? 'Activo' : 'Inactivo' }}
                  </span>
                </td>
                <td class="actions-cell">
                  <button class="btn-edit" (click)="iniciarEdicionMascota(p)">✏️ Editar</button>
                  <button class="btn-danger" (click)="eliminarMascota(p)">🗑 Eliminar</button>
                </td>
              </tr>

              <!-- Fila en modo edición -->
              <tr *ngIf="editandoMascotaId === p.id" class="fila-editando">
                <td>{{ p.id }}</td>
                <td>
                  <input class="input-edit" [(ngModel)]="petEdNombre" placeholder="Nombre" />
                </td>
                <td>
                  <select class="select-edit" [(ngModel)]="petEdEspecie">
                    <option value="perro">perro</option>
                    <option value="gato">gato</option>
                    <option value="ave">ave</option>
                    <option value="conejo">conejo</option>
                    <option value="pez">pez</option>
                    <option value="otro">otro</option>
                  </select>
                </td>
                <td>
                  <input class="input-edit" [(ngModel)]="petEdRaza" placeholder="Raza" />
                </td>
                <td>
                  <input class="input-edit" [(ngModel)]="petEdColor" placeholder="Color" />
                </td>
                <td>
                  <select class="select-edit" [(ngModel)]="petEdTamanio">
                    <option value="">—</option>
                    <option value="PEQUENIO">Pequeño</option>
                    <option value="MEDIANO">Mediano</option>
                    <option value="GRANDE">Grande</option>
                  </select>
                </td>
                <td>{{ p.ownerId }}</td>
                <td>
                  <span class="badge" [class.badge-active]="p.active" [class.badge-inactive]="!p.active">
                    {{ p.active ? 'Activo' : 'Inactivo' }}
                  </span>
                </td>
                <td class="actions-cell">
                  <button class="btn-save" (click)="guardarEdicionMascota(p)" [disabled]="guardandoMascota">
                    {{ guardandoMascota ? '...' : '💾 Guardar' }}
                  </button>
                  <button class="btn-cancel-edit" (click)="cancelarEdicionMascota()">✖ Cancelar</button>
                </td>
              </tr>

            </ng-container>
          </tbody>
        </table>
      </div>

      <!-- ═══ REPORTES ═══ -->
      <div *ngIf="tab === 'reportes'" class="table-card">
        <div *ngIf="loadingReportes" class="loading">Cargando reportes...</div>
        <table *ngIf="!loadingReportes">
          <thead>
            <tr>
              <th>ID</th>
              <th>Tipo</th>
              <th>Descripción</th>
              <th>Usuario ID</th>
              <th>Estado</th>
              <th>Fecha</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let r of reportes">
              <td>{{ r.id }}</td>
              <td>
                <span class="badge" [class.badge-perdido]="r.tipo==='PERDIDO'" [class.badge-encontrado]="r.tipo==='ENCONTRADO'">
                  {{ r.tipo }}
                </span>
              </td>
              <td class="desc">{{ r.descripcion }}</td>
              <td>{{ r.reporterUserId }}</td>
              <td>
                <select (change)="cambiarEstado(r, $event)" [value]="r.estado">
                  <option value="ACTIVO">ACTIVO</option>
                  <option value="RESUELTO">RESUELTO</option>
                  <option value="CERRADO">CERRADO</option>
                </select>
              </td>
              <td>{{ r.createdAt | date:'dd/MM/yyyy' }}</td>
              <td>
                <button class="btn-danger" (click)="eliminarReporte(r)">🗑 Eliminar</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Toast -->
      <div class="toast" [class.toast-visible]="toastVisible" [class.toast-error]="toastError">
        {{ toastMsg }}
      </div>

    </div>
  `,
  styles: [`
    .page{ padding:5rem 2rem 2rem; max-width:1300px; margin:0 auto }
    .admin-header{ display:flex; justify-content:space-between; align-items:center; margin-bottom:2rem }
    .admin-header h1{ font-size:1.8rem; color:#1a237e; margin:0 0 .25rem }
    .admin-header p{ color:#666; margin:0 }
    .badge-admin{ background:#ffc107; color:#333; padding:.2rem .6rem; border-radius:12px; font-size:.75rem; font-weight:700 }

    .stats-grid{ display:grid; grid-template-columns:repeat(4,1fr); gap:1rem; margin-bottom:2rem }
    .stat-card{ background:white; border-radius:12px; padding:1.5rem; text-align:center; box-shadow:0 2px 10px rgba(0,0,0,.08); display:flex; flex-direction:column; gap:.25rem }
    .stat-icon{ font-size:2rem }
    .stat-number{ font-size:2rem; font-weight:700; color:#1a237e }
    .stat-label{ font-size:.85rem; color:#666 }

    .tabs{ display:flex; gap:.5rem; margin-bottom:1rem }
    .tabs button{ padding:.6rem 1.2rem; border:2px solid #e0e0e0; border-radius:8px; background:white; cursor:pointer; font-weight:600; font-size:.9rem; color:#666 }
    .tabs button.active{ background:#1a237e; color:white; border-color:#1a237e }

    .table-card{ background:white; border-radius:12px; box-shadow:0 2px 10px rgba(0,0,0,.08); overflow:auto }
    table{ width:100%; border-collapse:collapse }
    th{ background:#f5f5f5; padding:.75rem 1rem; text-align:left; font-size:.8rem; color:#666; text-transform:uppercase; letter-spacing:.05em }
    td{ padding:.75rem 1rem; border-top:1px solid #f0f0f0; font-size:.9rem; vertical-align:middle }
    tr:hover td{ background:#fafafa }
    .fila-editando td{ background:#fffde7 !important }
    .desc{ max-width:200px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap }

    .badge{ padding:.2rem .6rem; border-radius:12px; font-size:.75rem; font-weight:600 }
    .badge-active{ background:#e8f5e9; color:#2e7d32 }
    .badge-inactive{ background:#ffebee; color:#c62828 }
    .badge-perdido{ background:#ffebee; color:#c62828 }
    .badge-encontrado{ background:#e8f5e9; color:#2e7d32 }

    .actions-cell{ display:flex; gap:.4rem; flex-wrap:wrap }

    .input-edit{ width:100%; padding:.35rem .6rem; border:1.5px solid #1a237e; border-radius:6px; font-size:.85rem; outline:none; box-sizing:border-box; min-width:80px }
    .select-edit{ padding:.35rem .6rem; border:1.5px solid #1a237e; border-radius:6px; font-size:.85rem; width:100%; min-width:90px }

    select{ padding:.3rem .6rem; border-radius:6px; border:1px solid #ddd; font-size:.85rem }

    .btn-edit{ padding:.3rem .7rem; background:#e8eaf6; color:#1a237e; border:none; border-radius:6px; cursor:pointer; font-size:.82rem; font-weight:600; white-space:nowrap }
    .btn-edit:hover{ background:#c5cae9 }

    .btn-toggle{ padding:.3rem .7rem; border:none; border-radius:6px; cursor:pointer; font-size:.82rem; font-weight:600; white-space:nowrap }
    .btn-deactivate{ background:#fff3e0; color:#e65100 }
    .btn-deactivate:hover{ background:#e65100; color:white }
    .btn-activate{ background:#e8f5e9; color:#2e7d32 }
    .btn-activate:hover{ background:#2e7d32; color:white }
    .btn-toggle:disabled{ opacity:.4; cursor:not-allowed }

    .btn-save{ padding:.3rem .7rem; background:#1a237e; color:white; border:none; border-radius:6px; cursor:pointer; font-size:.82rem; font-weight:600; white-space:nowrap }
    .btn-save:hover:not(:disabled){ background:#283593 }
    .btn-save:disabled{ opacity:.5; cursor:not-allowed }

    .btn-cancel-edit{ padding:.3rem .7rem; background:#f5f5f5; color:#555; border:none; border-radius:6px; cursor:pointer; font-size:.82rem; font-weight:600; white-space:nowrap }
    .btn-cancel-edit:hover{ background:#e0e0e0 }

    .btn-danger{ padding:.3rem .8rem; background:#ffebee; color:#c62828; border:none; border-radius:6px; cursor:pointer; font-size:.85rem; font-weight:600 }
    .btn-danger:hover{ background:#c62828; color:white }

    .loading{ text-align:center; padding:2rem; color:#999 }

    .toast{ position:fixed; bottom:2rem; right:2rem; background:#1a237e; color:white; padding:.85rem 1.4rem; border-radius:10px; font-size:.9rem; font-weight:600; box-shadow:0 4px 20px rgba(0,0,0,.2); opacity:0; transform:translateY(10px); transition:opacity .3s, transform .3s; pointer-events:none; z-index:999 }
    .toast-visible{ opacity:1; transform:translateY(0) }
    .toast-error{ background:#c62828 }

    @media(max-width:768px){
      .stats-grid{ grid-template-columns:repeat(2,1fr) }
      .page{ padding:5rem 1rem 1rem }
    }
  `]
})
export class AdminPanelComponent implements OnInit {

  tab = 'usuarios';

  usuarios: any[] = [];
  mascotas: any[] = [];
  reportes: any[] = [];

  loadingUsuarios = false;
  loadingMascotas = false;
  loadingReportes = false;

  // ── Edición de usuario ──────────────────────────────────────
  editandoUsuarioId: number | null = null;
  edicionNombre   = '';
  edicionEmail    = '';
  edicionTelefono = '';
  edicionRol      = '';
  guardando       = false;

  // ── Edición de mascota ──────────────────────────────────────
  editandoMascotaId: number | null = null;
  petEdNombre  = '';
  petEdEspecie = '';
  petEdRaza    = '';
  petEdColor   = '';
  petEdTamanio = '';
  guardandoMascota = false;

  // ── Toast ───────────────────────────────────────────────────
  toastMsg     = '';
  toastVisible = false;
  toastError   = false;
  private toastTimer: any;

  get currentUser() { return this.auth.getCurrentUser(); }
  get reportesActivos() { return this.reportes.filter(r => r.estado === 'ACTIVO').length; }

  constructor(
    private auth: AuthService,
    private petService: PetService,
    private reportService: ReportService,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit() {
    if (!this.auth.isAdmin()) {
      this.router.navigate(['/inicio']);
      return;
    }
    this.cargarDatos();
  }

  cargarDatos() {
    this.cargarUsuarios();
    this.cargarMascotas();
    this.cargarReportes();
  }

  cargarUsuarios() {
    this.loadingUsuarios = true;
    this.http.get<any[]>(`${environment.apiUrl}/users`).subscribe({
      next: (u) => { this.usuarios = u; this.loadingUsuarios = false; },
      error: () => { this.showToast('Error al cargar usuarios', true); this.loadingUsuarios = false; }
    });
  }

  cargarMascotas() {
    this.loadingMascotas = true;
    this.petService.getAll().subscribe({
      next: (p) => { this.mascotas = p; this.loadingMascotas = false; },
      error: () => { this.showToast('Error al cargar mascotas', true); this.loadingMascotas = false; }
    });
  }

  cargarReportes() {
    this.loadingReportes = true;
    this.reportService.getAll().subscribe({
      next: (r) => { this.reportes = r; this.loadingReportes = false; },
      error: () => { this.showToast('Error al cargar reportes', true); this.loadingReportes = false; }
    });
  }

  // ── Edición de usuario ──────────────────────────────────────

  iniciarEdicionUsuario(u: any) {
    this.cancelarEdicionMascota();
    this.editandoUsuarioId = u.id;
    this.edicionNombre   = u.nombre;
    this.edicionEmail    = u.email;
    this.edicionTelefono = u.telefono || '';
    this.edicionRol      = u.rol;
  }

  cancelarEdicion() {
    this.editandoUsuarioId = null;
  }

  guardarEdicionUsuario(u: any) {
    this.guardando = true;
    const payload = {
      nombre:   this.edicionNombre,
      email:    this.edicionEmail,
      telefono: this.edicionTelefono,
      rol:      this.edicionRol,
      active:   u.active
    };

    this.http.put<any>(`${environment.apiUrl}/users/${u.id}`, payload).subscribe({
      next: (actualizado) => {
        const idx = this.usuarios.findIndex(x => x.id === u.id);
        if (idx !== -1) this.usuarios[idx] = actualizado;
        this.editandoUsuarioId = null;
        this.guardando = false;
        this.showToast('✅ Usuario actualizado correctamente');
      },
      error: (err) => {
        const msg = err?.error?.message || 'Error al actualizar el usuario';
        this.showToast(msg, true);
        this.guardando = false;
      }
    });
  }

  toggleActivo(u: any) {
    const accion = u.active ? 'desactivar' : 'activar';
    if (!confirm(`¿Seguro que deseas ${accion} a ${u.nombre}?`)) return;

    const payload = { active: !u.active };
    this.http.put<any>(`${environment.apiUrl}/users/${u.id}`, payload).subscribe({
      next: (actualizado) => {
        const idx = this.usuarios.findIndex(x => x.id === u.id);
        if (idx !== -1) this.usuarios[idx] = actualizado;
        this.showToast(`✅ Usuario ${u.active ? 'desactivado' : 'activado'} correctamente`);
      },
      error: () => this.showToast('Error al cambiar estado del usuario', true)
    });
  }

  // ── Edición de mascota ──────────────────────────────────────

  iniciarEdicionMascota(p: any) {
    this.cancelarEdicion();
    this.editandoMascotaId = p.id;
    this.petEdNombre  = p.nombre;
    this.petEdEspecie = p.especie;
    this.petEdRaza    = p.raza    || '';
    this.petEdColor   = p.color   || '';
    this.petEdTamanio = p.tamanio || '';
  }

  cancelarEdicionMascota() {
    this.editandoMascotaId = null;
  }

  guardarEdicionMascota(p: any) {
    this.guardandoMascota = true;

    // El pet-service exige multipart/form-data
    const fd = new FormData();
    fd.append('nombre',   this.petEdNombre);
    fd.append('especie',  this.petEdEspecie);
    fd.append('raza',     this.petEdRaza);
    fd.append('color',    this.petEdColor);
    fd.append('tamanio',  this.petEdTamanio);
    fd.append('descripcion', p.descripcion || '');

    this.petService.update(p.id, fd).subscribe({
      next: (actualizado) => {
        const idx = this.mascotas.findIndex(m => m.id === p.id);
        if (idx !== -1) this.mascotas[idx] = { ...this.mascotas[idx], ...actualizado };
        this.editandoMascotaId = null;
        this.guardandoMascota  = false;
        this.showToast('✅ Mascota actualizada correctamente');
      },
      error: (err) => {
        const msg = err?.error?.message || 'Error al actualizar la mascota';
        this.showToast(msg, true);
        this.guardandoMascota = false;
      }
    });
  }

  // ── Mascotas — eliminar ─────────────────────────────────────

  eliminarMascota(pet: any) {
    if (!confirm(`¿Eliminar a ${pet.nombre}?`)) return;
    this.petService.delete(pet.id).subscribe({
      next: () => {
        this.mascotas = this.mascotas.filter(p => p.id !== pet.id);
        this.showToast('✅ Mascota eliminada');
      },
      error: () => this.showToast('Error al eliminar la mascota', true)
    });
  }

  // ── Reportes ────────────────────────────────────────────────

  eliminarReporte(reporte: any) {
    if (!confirm(`¿Eliminar reporte #${reporte.id}?`)) return;
    this.reportService.delete(reporte.id).subscribe({
      next: () => {
        this.reportes = this.reportes.filter(r => r.id !== reporte.id);
        this.showToast('✅ Reporte eliminado');
      },
      error: () => this.showToast('Error al eliminar el reporte', true)
    });
  }

  cambiarEstado(reporte: any, event: any) {
    const estadoAnterior = reporte.estado;
    const nuevoEstado = event.target.value;
    this.reportService.updateEstado(reporte.id, nuevoEstado).subscribe({
      next: (r) => {
        reporte.estado = r.estado;
        this.showToast(`✅ Estado cambiado a ${r.estado}`);
      },
      error: (e) => {
        console.error('Error cambiar estado:', e.status, e.error);
        event.target.value = estadoAnterior;
        this.showToast(`Error ${e.status}: ${e.error?.message || e.message || 'desconocido'}`, true);
      }
    });
  }

  // ── Toast ───────────────────────────────────────────────────

  private showToast(msg: string, isError = false) {
    clearTimeout(this.toastTimer);
    this.toastMsg     = msg;
    this.toastError   = isError;
    this.toastVisible = true;
    this.toastTimer   = setTimeout(() => this.toastVisible = false, 3000);
  }
}