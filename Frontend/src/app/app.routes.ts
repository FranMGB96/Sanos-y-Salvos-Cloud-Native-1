import { Routes } from '@angular/router';
import { authGuard }  from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'inicio', pathMatch: 'full' },
  { path: 'login',    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent) },
  { path: 'inicio',        canActivate: [authGuard],  loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
  { path: 'pets',          canActivate: [authGuard],  loadComponent: () => import('./features/pets/pet-list/pet-list.component').then(m => m.PetListComponent) },
  { path: 'pets/new',      canActivate: [authGuard],  loadComponent: () => import('./features/pets/pet-form/pet-form.component').then(m => m.PetFormComponent) },
  { path: 'pets/edit/:id', canActivate: [authGuard],  loadComponent: () => import('./features/pets/pet-form/pet-form.component').then(m => m.PetFormComponent) },
  { path: 'reports',       canActivate: [authGuard],  loadComponent: () => import('./features/reports/report-list/report-list.component').then(m => m.ReportListComponent) },
  { path: 'reports/new',   canActivate: [authGuard],  loadComponent: () => import('./features/reports/report-form/report-form.component').then(m => m.ReportFormComponent) },
  { path: 'nosotros',      loadComponent: () => import('./features/about/about.component').then(m => m.AboutComponent) },
  { path: 'admin',         canActivate: [adminGuard], loadComponent: () => import('./features/admin/admin-panel.component').then(m => m.AdminPanelComponent) },
  // ✅ Perfil de usuario
  { path: 'perfil',        canActivate: [authGuard],  loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent) },
  { path: '**', redirectTo: 'inicio' },
];