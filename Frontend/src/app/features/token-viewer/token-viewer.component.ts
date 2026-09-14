import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MsalService } from '@azure/msal-angular';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-token-viewer',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="token-viewer">
      <h1>🔑 Información del Token JWT</h1>
      <p class="subtitle">Token de acceso obtenido de Azure AD (Microsoft Entra ID) para las llamadas a la API.</p>

      <button class="btn-refresh" (click)="cargarToken()">🔄 Refrescar Token</button>

      <div *ngIf="error" class="error-box">{{ error }}</div>

      <div *ngIf="claims" class="claims-card">
        <h2>Claims decodificados</h2>
        <table class="claims-table">
          <tr><td>Issuer (iss)</td><td>{{ claims.iss }}</td></tr>
          <tr><td>Audience (aud)</td><td>{{ claims.aud }}</td></tr>
          <tr><td>Scope (scp)</td><td>{{ claims.scp }}</td></tr>
          <tr><td>Subject (sub)</td><td>{{ claims.sub }}</td></tr>
          <tr><td>Nombre</td><td>{{ claims.name }}</td></tr>
          <tr><td>Versión (ver)</td><td>{{ claims.ver }}</td></tr>
          <tr><td>Emitido (iat)</td><td>{{ formatDate(claims.iat) }}</td></tr>
          <tr><td>Expira (exp)</td><td>{{ formatDate(claims.exp) }}</td></tr>
        </table>
      </div>

      <div *ngIf="rawToken" class="raw-token-card">
        <h2>Token completo (raw)</h2>
        <textarea readonly rows="6">{{ rawToken }}</textarea>
      </div>
    </div>
  `,
  styles: [`
    .token-viewer{max-width:900px;margin:6rem auto 2rem;padding:0 1.5rem}
    h1{color:#1a237e;margin-bottom:.25rem}
    .subtitle{color:#666;margin-bottom:1.5rem}
    .btn-refresh{background:#1a237e;color:#fff;border:none;border-radius:6px;padding:.6rem 1.2rem;cursor:pointer;font-size:.95rem;margin-bottom:1.5rem}
    .btn-refresh:hover{background:#283593}
    .error-box{background:#ffebee;color:#c62828;padding:1rem;border-radius:8px;margin-bottom:1rem}
    .claims-card,.raw-token-card{background:#fff;border-radius:10px;padding:1.5rem;box-shadow:0 2px 8px rgba(0,0,0,.08);margin-bottom:1.5rem}
    .claims-table{width:100%;border-collapse:collapse}
    .claims-table td{padding:.6rem .5rem;border-bottom:1px solid #eee;font-size:.9rem}
    .claims-table td:first-child{font-weight:600;color:#1a237e;width:180px}
    textarea{width:100%;font-family:monospace;font-size:.75rem;border:1px solid #ddd;border-radius:6px;padding:.75rem;resize:vertical;word-break:break-all}
  `]
})
export class TokenViewerComponent implements OnInit {
  claims: any = null;
  rawToken: string | null = null;
  error: string | null = null;

  constructor(private msal: MsalService) {}

  ngOnInit() {
    this.cargarToken();
  }

  cargarToken() {
    this.error = null;
    const account = this.msal.instance.getActiveAccount() || this.msal.instance.getAllAccounts()[0];
    if (!account) {
      this.error = 'No hay una cuenta activa. Inicia sesión de nuevo.';
      return;
    }
    this.msal.instance.acquireTokenSilent({
      scopes: environment.azure.scopes,
      account,
    }).then(result => {
      this.rawToken = result.accessToken;
      this.claims = this.decodeJwt(result.accessToken);
    }).catch(err => {
      this.error = 'No se pudo obtener el token: ' + (err?.message || err);
    });
  }

  decodeJwt(token: string): any {
    const payload = token.split('.')[1];
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(decoded);
  }

  formatDate(unixSeconds: number): string {
    if (!unixSeconds) return '-';
    return new Date(unixSeconds * 1000).toLocaleString('es-CL');
  }
}
