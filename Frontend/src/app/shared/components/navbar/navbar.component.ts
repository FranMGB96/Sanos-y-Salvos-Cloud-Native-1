import { Component } from '@angular/core';

import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CommonModule } from '@angular/common';



@Component({
  selector: 'app-navbar', standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  template: `
    <nav class="navbar">
      <div class="brand">
        <span class="logo">🐾</span>
        <span class="title">Sanos y Salvos</span>
      </div>
      <div class="links">
        <a routerLink="/inicio"   routerLinkActive="active">Inicio</a>
        <a routerLink="/pets"     routerLinkActive="active">Mascotas</a>
        <a routerLink="/reports"  routerLinkActive="active">Reportes</a>
        <a *ngIf="auth.isAdmin()" routerLink="/admin" routerLinkActive="active" class="admin-link">
          ⚙️ Panel Admin
        </a>
      </div>
      <div class="user-info">
        <!-- ✅ Nombre clickeable → perfil -->
        <a routerLink="/perfil" class="user-name" routerLinkActive="active-user">
          👤 {{ user?.nombre }}
        </a>
        <span class="user-role" [class.role-admin]="auth.isAdmin()">
          {{ user?.rol }}
        </span>
        <button class="btn-logout" (click)="logout()">Salir</button>
      </div>
    </nav>`,
  styles: [`
    .navbar{position:fixed;top:0;left:0;right:0;z-index:100;display:flex;align-items:center;justify-content:space-between;height:64px;padding:0 2rem;background:#1a237e;color:white;box-shadow:0 2px 8px rgba(0,0,0,.2)}
    .brand{display:flex;align-items:center;gap:.5rem}
    .logo{font-size:1.5rem}
    .title{font-size:1.2rem;font-weight:700}
    .links{display:flex;gap:1.5rem}
    .links a{color:rgba(255,255,255,.8);text-decoration:none;padding:.3rem .6rem;border-radius:4px;font-size:.95rem;transition:background .2s}
    .links a:hover,.links a.active{color:white;background:rgba(255,255,255,.15)}
    .admin-link{background:rgba(255,193,7,.2);color:#ffc107 !important;border:1px solid rgba(255,193,7,.4);border-radius:6px}
    .admin-link:hover,.admin-link.active{background:rgba(255,193,7,.35) !important}
    .user-info{display:flex;align-items:center;gap:.75rem}
    .user-name{
      font-weight:600;font-size:.9rem;color:rgba(255,255,255,.9);
      text-decoration:none;padding:.3rem .6rem;border-radius:6px;
      transition:background .2s;cursor:pointer;
    }
    .user-name:hover,.active-user{background:rgba(255,255,255,.15);color:white}
    .user-role{font-size:.75rem;background:rgba(255,255,255,.2);padding:.2rem .5rem;border-radius:12px}
    .role-admin{background:rgba(255,193,7,.3);color:#ffc107;font-weight:700}
    .btn-logout{background:rgba(255,255,255,.15);color:white;border:1px solid rgba(255,255,255,.3);border-radius:6px;padding:.3rem .8rem;cursor:pointer;font-size:.85rem}
    .btn-logout:hover{background:rgba(255,255,255,.25)}
  `]
})
export class NavbarComponent {
  get user() { return this.auth.getCurrentUser(); }
  constructor(public auth: AuthService) {}
  logout() { this.auth.logout(); }
}
