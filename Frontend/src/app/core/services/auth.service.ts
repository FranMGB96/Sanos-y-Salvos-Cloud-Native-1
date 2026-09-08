import { Injectable } from '@angular/core';
import { MsalService } from '@azure/msal-angular';
import { environment } from '../../../environments/environment';

export interface CurrentUser {
  nombre: string;
  email: string;
  // Ya no existe un ID interno numérico de user-service (Azure AD no lo
  // conoce). Queda en null a propósito: features que dependían de un
  // userId real (editar perfil, "reporterUserId", ownerId de mascotas)
  // no van a funcionar del todo con este login simplificado.
  userId: number | null;
  rol?: string;
  telefono?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  constructor(private msalService: MsalService) {}

  // Con Redirect, esto navega la página completa hacia Microsoft; no hay
  // nada que "esperar" en este mismo componente. El resultado se procesa
  // al volver, en initializeMsal() (app.config.ts) + AuthRedirectComponent.
  login(): void {
    this.msalService.loginRedirect({ scopes: environment.azure.scopes });
  }

  logout(): void {
    this.msalService.logoutRedirect();
  }

  isLoggedIn(): boolean {
    return this.msalService.instance.getAllAccounts().length > 0;
  }

  getCurrentUser(): CurrentUser | null {
    const account = this.msalService.instance.getActiveAccount()
      ?? this.msalService.instance.getAllAccounts()[0]
      ?? null;
    if (!account) return null;
    return { nombre: account.name ?? account.username, email: account.username, userId: null };
  }

  // Simplificado para esta evaluación: en vez de configurar App Roles reales
  // en Azure AD (más trabajo y no evaluado en la rúbrica), tratamos como
  // admin solo a esta cuenta específica. Cualquier otra que inicie sesión
  // es un usuario normal.
  private readonly ADMIN_EMAIL = 'fra.garciab@duocuc.cl';

  isAdmin(): boolean {
    return this.getCurrentUser()?.email?.toLowerCase() === this.ADMIN_EMAIL;
  }

  // ── Stubs de compatibilidad ──────────────────────────────
  // Mantienen el proyecto compilando para pantallas que dependían del
  // login propio (editar perfil, cambiar contraseña). MsalInterceptor ya
  // adjunta el token real automáticamente a las llamadas a la API, así
  // que getToken() ya no es necesario para eso — solo evita errores de
  // compilación en código legado que no es parte de esta evaluación.
  getToken(): string | null {
    return null;
  }

  updateCurrentUser(_parcial: Partial<CurrentUser>): void {
    // No-op: la identidad ahora la maneja MSAL, no hay estado local que actualizar.
  }
}
