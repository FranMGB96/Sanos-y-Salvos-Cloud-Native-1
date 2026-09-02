import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../core/services/auth.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="page">

      <div class="page-header">
        <div class="avatar">{{ inicial }}</div>
        <div>
          <h1>Mi Perfil</h1>
          <p>{{ user?.email }}</p>
        </div>
      </div>

      <!-- DATOS PERSONALES -->
      <div class="card">
        <h2>Datos personales</h2>

        <div class="alert-success" *ngIf="successDatos">✅ Datos actualizados correctamente</div>
        <div class="alert-error"   *ngIf="errorDatos">{{ errorDatos }}</div>

        <form [formGroup]="datosForm" (ngSubmit)="guardarDatos()">

          <div class="field">
            <label>Nombre completo</label>
            <input type="text" formControlName="nombre"
              [class.error]="f('nombre').invalid && f('nombre').touched">
            <span class="err" *ngIf="f('nombre').invalid && f('nombre').touched">Obligatorio</span>
          </div>

          <div class="field">
            <label>Email</label>
            <input type="email" formControlName="email"
              [class.error]="f('email').invalid && f('email').touched">
            <span class="err" *ngIf="f('email').invalid && f('email').touched">Email válido requerido</span>
          </div>

          <div class="field">
            <label>Teléfono</label>
            <div class="phone-wrapper">
              <span class="phone-prefix">📞 +56</span>
              <input type="tel" formControlName="telefono"
                placeholder="9 1234 5678"
                [class.error]="f('telefono').invalid && f('telefono').touched">
            </div>
            <span class="err" *ngIf="f('telefono').invalid && f('telefono').touched">Mínimo 8 dígitos</span>
          </div>

          <button type="submit" [disabled]="loadingDatos" class="btn-primary">
            {{ loadingDatos ? 'Guardando...' : 'Guardar cambios' }}
          </button>

        </form>
      </div>

      <!-- CAMBIAR CONTRASEÑA -->
      <div class="card">
        <h2>Cambiar contraseña</h2>

        <div class="alert-success" *ngIf="successPass">✅ Contraseña actualizada correctamente</div>
        <div class="alert-error"   *ngIf="errorPass">{{ errorPass }}</div>

        <form [formGroup]="passForm" (ngSubmit)="cambiarPassword()">

          <div class="field">
            <label>Nueva contraseña</label>
            <input type="password" formControlName="password" placeholder="Mínimo 6 caracteres"
              [class.error]="p('password').invalid && p('password').touched">
            <span class="err" *ngIf="p('password').invalid && p('password').touched">Mínimo 6 caracteres</span>
          </div>

          <div class="field">
            <label>Confirmar contraseña</label>
            <input type="password" formControlName="confirmar" placeholder="Repite la contraseña"
              [class.error]="p('confirmar').invalid && p('confirmar').touched">
            <span class="err" *ngIf="passForm.errors?.['noCoincide'] && p('confirmar').touched">
              Las contraseñas no coinciden
            </span>
          </div>

          <button type="submit" [disabled]="loadingPass" class="btn-secondary">
            {{ loadingPass ? 'Actualizando...' : 'Cambiar contraseña' }}
          </button>

        </form>
      </div>

    </div>
  `,
  styles: [`
    .page{
      padding:2rem;
      max-width:600px;
      margin:0 auto;
    }

    .page-header{
      display:flex;
      align-items:center;
      gap:1.25rem;
      margin-bottom:2rem;
    }

    .avatar{
      width:64px; height:64px;
      background:#1a237e; color:white;
      border-radius:50%;
      display:flex; align-items:center; justify-content:center;
      font-size:1.8rem; font-weight:700;
      flex-shrink:0;
    }

    .page-header h1{ font-size:1.6rem; color:#1a237e; margin:0 0 .2rem; }
    .page-header p { color:#888; margin:0; font-size:.9rem; }

    .card{
      background:white;
      border-radius:16px;
      padding:1.75rem;
      margin-bottom:1.5rem;
      box-shadow:0 2px 10px rgba(0,0,0,.07);
    }

    h2{
      font-size:1.1rem;
      color:#222;
      margin:0 0 1.5rem;
      padding-bottom:.75rem;
      border-bottom:1px solid #eee;
    }

    .field{ margin-bottom:1.2rem; }

    label{
      display:block;
      font-size:.85rem;
      font-weight:600;
      color:#333;
      margin-bottom:.4rem;
    }

    input{
      width:100%;
      padding:.75rem 1rem;
      border:1.5px solid #ddd;
      border-radius:8px;
      font-size:.95rem;
      box-sizing:border-box;
      outline:none;
      transition:border .2s;
      font-family:inherit;
    }

    input:focus{ border-color:#1a237e; }
    input.error{ border-color:#e53935; }

    .phone-wrapper{
      display:flex;
      align-items:center;
      border:1.5px solid #ddd;
      border-radius:8px;
      overflow:hidden;
      transition:border .2s;
    }

    .phone-wrapper:focus-within{ border-color:#1a237e; }

    .phone-prefix{
      padding:.75rem .85rem;
      background:#f0f4ff;
      color:#1a237e;
      font-size:.9rem;
      font-weight:600;
      white-space:nowrap;
      border-right:1.5px solid #ddd;
    }

    .phone-wrapper input{
      border:none;
      border-radius:0;
      flex:1;
    }

    .phone-wrapper input:focus{ outline:none; }

    .err{
      color:#e53935;
      font-size:.78rem;
      margin-top:.25rem;
      display:block;
    }

    .btn-primary{
      width:100%;
      padding:.85rem;
      background:#1a237e;
      color:white;
      border:none;
      border-radius:8px;
      font-size:1rem;
      font-weight:600;
      cursor:pointer;
      transition:background .2s;
      margin-top:.5rem;
    }

    .btn-primary:hover{ background:#283593; }
    .btn-primary:disabled{ opacity:.6; cursor:not-allowed; }

    .btn-secondary{
      width:100%;
      padding:.85rem;
      background:white;
      color:#1a237e;
      border:2px solid #1a237e;
      border-radius:8px;
      font-size:1rem;
      font-weight:600;
      cursor:pointer;
      transition:all .2s;
      margin-top:.5rem;
    }

    .btn-secondary:hover{ background:#f0f4ff; }
    .btn-secondary:disabled{ opacity:.6; cursor:not-allowed; }

    .alert-success{
      background:#e8f5e9; color:#2e7d32;
      border-radius:8px; padding:.75rem 1rem;
      font-size:.88rem; margin-bottom:1rem;
    }

    .alert-error{
      background:#ffebee; color:#c62828;
      border-radius:8px; padding:.75rem 1rem;
      font-size:.88rem; margin-bottom:1rem;
    }
  `]
})
export class ProfileComponent implements OnInit {

  datosForm!: FormGroup;
  passForm!: FormGroup;

  loadingDatos = false;
  loadingPass  = false;
  successDatos = false;
  successPass  = false;
  errorDatos   = '';
  errorPass    = '';

  get user()    { return this.auth.getCurrentUser(); }
  get inicial() { return this.user?.nombre?.charAt(0).toUpperCase() ?? '?'; }

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.datosForm = this.fb.group({
      nombre:   [this.user?.nombre   ?? '', Validators.required],
      email:    [this.user?.email    ?? '', [Validators.required, Validators.email]],
      telefono: [this.user?.telefono ?? '', [Validators.required, Validators.minLength(8)]]
    });

    this.passForm = this.fb.group({
      password:  ['', [Validators.required, Validators.minLength(6)]],
      confirmar: ['', Validators.required]
    }, { validators: this.passwordsIguales });
  }

  f(name: string) { return this.datosForm.get(name)!; }
  p(name: string) { return this.passForm.get(name)!; }

  passwordsIguales(group: FormGroup) {
    const pass = group.get('password')?.value;
    const conf = group.get('confirmar')?.value;
    return pass === conf ? null : { noCoincide: true };
  }

  guardarDatos() {
  if (this.datosForm.invalid) { this.datosForm.markAllAsTouched(); return; }
  this.loadingDatos = true;
  this.successDatos = false;
  this.errorDatos   = '';

  const userId  = this.user?.userId;
  const token   = this.auth.getToken();
  const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });

  const body = {
    nombre:   this.f('nombre').value,
    email:    this.f('email').value,
    telefono: this.f('telefono').value.replace(/\s/g, '')
  };

  this.http.put(`${environment.apiUrl}/users/${userId}`, body, { headers }).subscribe({
    next: () => {
      // ✅ Actualiza navbar y localStorage automáticamente
      this.auth.updateCurrentUser({
        nombre:   body.nombre,
        email:    body.email,
        telefono: body.telefono
      });
      this.successDatos = true;
      this.loadingDatos = false;
      setTimeout(() => this.successDatos = false, 3000);
    },
    error: (e) => {
      this.errorDatos   = e.error?.message || 'Error al actualizar datos.';
      this.loadingDatos = false;
    }
  });
}

  cambiarPassword() {
    if (this.passForm.invalid) { this.passForm.markAllAsTouched(); return; }
    this.loadingPass = true;
    this.successPass = false;
    this.errorPass   = '';

    const userId = this.user?.userId;
    const token  = this.auth.getToken();
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });

    this.http.put(`${environment.apiUrl}/users/${userId}`, {
      password: this.p('password').value
    }, { headers }).subscribe({
      next: () => {
        this.successPass = true;
        this.loadingPass = false;
        this.passForm.reset();
        setTimeout(() => this.successPass = false, 3000);
      },
      error: (e) => {
        this.errorPass   = e.error?.message || 'Error al cambiar contraseña.';
        this.loadingPass = false;
      }
    });
  }
}