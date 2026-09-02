import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-card">

        <!-- ══ PANTALLA DE ÉXITO ══ -->
        <div *ngIf="registrado" class="success-screen">
          <div class="success-icon">🎉</div>
          <h1>¡Gracias, {{ nombreRegistrado }}!</h1>
          <p class="success-msg">Tu cuenta fue creada exitosamente.<br>Ya puedes iniciar sesión.</p>
          <a routerLink="/login" class="btn-login">Iniciar sesión →</a>
        </div>

        <!-- ══ FORMULARIO ══ -->
        <ng-container *ngIf="!registrado">

          <div class="auth-header">
            <span class="logo">🐾</span>
            <h1>Crear Cuenta</h1>
            <p>Únete a Sanos y Salvos</p>
          </div>

          <form [formGroup]="form" (ngSubmit)="onSubmit()">

            <div class="field">
              <label>Nombre completo *</label>
              <input
                type="text"
                formControlName="nombre"
                placeholder="Tu nombre"
                [class.error]="f('nombre').invalid && f('nombre').touched"
              >
              <span class="err" *ngIf="f('nombre').invalid && f('nombre').touched">
                Obligatorio
              </span>
            </div>

            <div class="field">
              <label>Email *</label>
              <input
                type="email"
                formControlName="email"
                placeholder="tu@email.com"
                [class.error]="f('email').invalid && f('email').touched"
              >
              <span class="err" *ngIf="f('email').invalid && f('email').touched">
                Email válido requerido
              </span>
            </div>

            <div class="field">
              <label>Teléfono de contacto *</label>
              <div class="phone-wrapper">
                <span class="phone-prefix">📞 +56</span>
                <input
                  type="tel"
                  formControlName="telefono"
                  placeholder="9 1234 5678"
                  [class.error]="f('telefono').invalid && f('telefono').touched"
                >
              </div>
              <span class="err" *ngIf="f('telefono').invalid && f('telefono').touched">
                Teléfono válido requerido (mínimo 8 dígitos)
              </span>
              <span class="field-hint">
                Este número se mostrará en tus reportes para que puedan contactarte
              </span>
            </div>

            <div class="field">
              <label>Contraseña *</label>
              <input
                type="password"
                formControlName="password"
                placeholder="Mínimo 6 caracteres"
                [class.error]="f('password').invalid && f('password').touched"
              >
              <span class="err" *ngIf="f('password').invalid && f('password').touched">
                Mínimo 6 caracteres
              </span>
            </div>

            <div class="alert" *ngIf="errorMsg">{{ errorMsg }}</div>

            <button type="submit" [disabled]="loading">
              {{ loading ? 'Registrando...' : 'Registrarse' }}
            </button>

          </form>

          <p class="footer-link">
            ¿Ya tienes cuenta? <a routerLink="/login">Inicia sesión</a>
          </p>

        </ng-container>

      </div>
    </div>
  `,
  styles: [`
    .auth-page{
      min-height:100vh;
      display:flex;
      align-items:center;
      justify-content:center;
      background:linear-gradient(135deg,#1a237e 0%,#283593 100%);
    }

    .auth-card{
      background:white;
      border-radius:16px;
      padding:2.5rem;
      width:100%;
      max-width:420px;
      box-shadow:0 20px 60px rgba(0,0,0,.2);
    }

    .auth-header{
      text-align:center;
      margin-bottom:2rem;
    }

    .logo{ font-size:3rem; }

    h1{
      margin:.5rem 0 .25rem;
      color:#1a237e;
      font-size:1.6rem;
    }

    .auth-header p{
      color:#666;
      font-size:.9rem;
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
      background:white;
      transition:border .2s;
      font-family:inherit;
    }

    input:focus{ border-color:#1a237e; }
    input.error{ border-color:#e53935; }

    /* TELÉFONO */
    .phone-wrapper{
      display:flex;
      align-items:center;
      border:1.5px solid #ddd;
      border-radius:8px;
      overflow:hidden;
      transition:border .2s;
    }

    .phone-wrapper:focus-within{ border-color:#1a237e; }
    .phone-wrapper input.error{ border:none; }

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

    .phone-wrapper input:focus{ border:none; outline:none; }

    .err{
      color:#e53935;
      font-size:.78rem;
      margin-top:.25rem;
      display:block;
    }

    .field-hint{
      display:block;
      font-size:.78rem;
      color:#888;
      margin-top:.3rem;
    }

    .alert{
      background:#ffebee;
      color:#c62828;
      border-radius:8px;
      padding:.75rem 1rem;
      font-size:.85rem;
      margin-bottom:1rem;
    }

    button{
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
    }

    button:hover{ background:#283593; }
    button:disabled{ opacity:.6; cursor:not-allowed; }

    .footer-link{
      text-align:center;
      margin-top:1.5rem;
      font-size:.9rem;
      color:#666;
    }

    .footer-link a{
      color:#1a237e;
      font-weight:600;
    }

    /* ── Pantalla de éxito ── */
    .success-screen{
      text-align:center;
      padding: 1rem 0 .5rem;
      display:flex;
      flex-direction:column;
      align-items:center;
      gap:1rem;
    }

    .success-icon{
      font-size:4rem;
      animation: pop .4s ease-out;
    }

    @keyframes pop{
      0%  { transform:scale(0); opacity:0; }
      80% { transform:scale(1.15); }
      100%{ transform:scale(1);  opacity:1; }
    }

    .success-screen h1{
      font-size:1.6rem;
      color:#1a237e;
      margin:0;
    }

    .success-msg{
      color:#555;
      font-size:.95rem;
      line-height:1.6;
      margin:0;
    }

    .btn-login{
      margin-top:.5rem;
      display:inline-block;
      background:#1a237e;
      color:white;
      padding:.85rem 2.5rem;
      border-radius:8px;
      font-size:1rem;
      font-weight:700;
      text-decoration:none;
      transition:background .2s, transform .15s;
    }

    .btn-login:hover{
      background:#283593;
      transform:translateY(-2px);
    }
  `]
})
export class RegisterComponent {
  form: FormGroup;
  loading = false;
  errorMsg = '';
  registrado = false;
  nombreRegistrado = '';

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      nombre:   ['', Validators.required],
      email:    ['', [Validators.required, Validators.email]],
      telefono: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^[0-9\s\-\+]+$/)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  f(name: string) { return this.form.get(name)!; }

  onSubmit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.errorMsg = '';

    const raw = this.form.value;
    const payload = {
      ...raw,
      telefono: raw.telefono.replace(/\s/g, '')
    };

    this.auth.register(payload).subscribe({
      next: () => {
        // Limpiar sesión sin redirigir (logout() redirige a /login automáticamente)
        localStorage.removeItem('sns_token');
        localStorage.removeItem('sns_user');
        this.nombreRegistrado = raw.nombre.split(' ')[0];
        this.registrado = true;
        this.loading = false;
      },
      error: (e: any) => {
        this.errorMsg = e.error?.message || 'Error al registrar.';
        this.loading = false;
      }
    });
  }
}