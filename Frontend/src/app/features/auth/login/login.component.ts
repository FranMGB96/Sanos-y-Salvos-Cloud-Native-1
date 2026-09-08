import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="auth-page">
      <div class="auth-card">
        <div class="auth-header">
          <span class="logo">🐾</span>
          <h1>Sanos y Salvos</h1>
          <p>Inicia sesión con tu cuenta de Microsoft</p>
        </div>

        <div class="alert" *ngIf="errorMsg">
          {{ errorMsg }}
        </div>

        <button (click)="loginConMicrosoft()" [disabled]="loading">
          <span class="ms-icon">⊞</span>
          {{ loading ? 'Ingresando...' : 'Iniciar sesión con Microsoft' }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .auth-page {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #1a237e 0%, #283593 100%);
    }

    .auth-card {
      background: white;
      border-radius: 16px;
      padding: 2.5rem;
      width: 100%;
      max-width: 420px;
      box-shadow: 0 20px 60px rgba(0, 0, 0, .2);
    }

    .auth-header {
      text-align: center;
      margin-bottom: 2rem;
    }

    .logo {
      font-size: 3rem;
    }

    h1 {
      margin: .5rem 0 .25rem;
      color: #1a237e;
      font-size: 1.6rem;
    }

    .auth-header p {
      color: #666;
      font-size: .9rem;
    }

    .alert {
      background: #ffebee;
      color: #c62828;
      border-radius: 8px;
      padding: .75rem 1rem;
      font-size: .85rem;
      margin-bottom: 1rem;
    }

    button {
      width: 100%;
      padding: .85rem;
      background: #1a237e;
      color: white;
      border: none;
      border-radius: 8px;
      font-size: 1rem;
      font-weight: 600;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: .6rem;
    }

    button:disabled {
      opacity: .6;
      cursor: not-allowed;
    }

    .ms-icon {
      font-size: 1.2rem;
    }
  `]
})
export class LoginComponent implements OnInit {

  loading = false;
  errorMsg = '';

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit() {
    // Si el usuario ya tiene sesión activa (ej. volvió a /login manualmente,
    // con el botón "atrás" del navegador, o por un enlace viejo) no tiene
    // sentido mostrarle el login de nuevo.
    if (this.auth.isLoggedIn()) {
      this.router.navigate(['/inicio']);
    }
  }

  loginConMicrosoft() {
    this.loading = true;
    this.errorMsg = '';
    // login() navega la página completa hacia Microsoft; no hay nada más
    // que hacer aquí. Si algo sale mal antes de salir de la página (ej.
    // configuración inválida), MSAL lanza el error de forma síncrona.
    try {
      this.auth.login();
    } catch (err) {
      console.error('Error al iniciar sesión con Microsoft', err);
      this.loading = false;
      this.errorMsg = 'No se pudo iniciar sesión. Intenta de nuevo.';
    }
  }
}
