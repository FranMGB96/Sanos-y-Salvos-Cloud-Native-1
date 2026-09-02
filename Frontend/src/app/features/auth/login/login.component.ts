import { Component } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterLink
  ],
  template: `
    <div class="auth-page">
      <div class="auth-card">
        <div class="auth-header">
          <span class="logo">🐾</span>
          <h1>Sanos y Salvos</h1>
          <p>Inicia sesión en tu cuenta</p>
        </div>

        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="field">
            <label>Email</label>
            <input
              type="email"
              formControlName="email"
              placeholder="tu@email.com"
              [class.error]="f('email').invalid && f('email').touched"
            >
          </div>

          <div class="field">
            <label>Contraseña</label>
            <input
              type="password"
              formControlName="password"
              placeholder="••••••"
              [class.error]="f('password').invalid && f('password').touched"
            >
          </div>

          <div class="alert" *ngIf="errorMsg">
            {{ errorMsg }}
          </div>

          <button
            type="submit"
            [disabled]="loading"
          >
            {{ loading ? 'Ingresando...' : 'Ingresar' }}
          </button>
        </form>

        <p class="footer-link">
          ¿No tienes cuenta?
          <a routerLink="/register">Regístrate</a>
        </p>
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

    .field {
      margin-bottom: 1.2rem;
    }

    label {
      display: block;
      font-size: .85rem;
      font-weight: 600;
      color: #333;
      margin-bottom: .4rem;
    }

    input {
      width: 100%;
      padding: .75rem 1rem;
      border: 1.5px solid #ddd;
      border-radius: 8px;
      font-size: .95rem;
      box-sizing: border-box;
      outline: none;
      transition: border .2s;
    }

    input:focus {
      border-color: #1a237e;
    }

    input.error {
      border-color: #e53935;
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
    }

    button:disabled {
      opacity: .6;
      cursor: not-allowed;
    }

    .footer-link {
      text-align: center;
      margin-top: 1.5rem;
      font-size: .9rem;
      color: #666;
    }

    .footer-link a {
      color: #1a237e;
      font-weight: 600;
    }
  `]
})
export class LoginComponent {

  form: FormGroup;
  loading = false;
  errorMsg = '';

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      email: [
        '',
        [
          Validators.required,
          Validators.email
        ]
      ],
      password: [
        '',
        [
          Validators.required,
          Validators.minLength(6)
        ]
      ]
    });
  }

  f(n: string) {
    return this.form.get(n)!;
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMsg = '';

    this.auth.login(this.form.value).subscribe({
      next: () => this.router.navigate(['/inicio']),
      error: () => {
        this.errorMsg = 'Email o contraseña incorrectos';
        this.loading = false;
      }
    });
  }
}