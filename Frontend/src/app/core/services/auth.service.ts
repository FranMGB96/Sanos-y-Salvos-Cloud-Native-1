import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AuthRequest, AuthResponse, RegisterRequest } from '../models/user.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'sns_token';
  private readonly USER_KEY  = 'sns_user';
  private apiUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(this.loadUser());
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  login(request: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(tap(r => this.save(r)));
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request).pipe(tap(r => this.save(r)));
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null { return localStorage.getItem(this.TOKEN_KEY); }
  isLoggedIn(): boolean { return !!this.getToken(); }
  getCurrentUser(): AuthResponse | null { return this.currentUserSubject.value; }

  // ✅ Actualizar datos del usuario en memoria y localStorage
  updateCurrentUser(parcial: Partial<AuthResponse>): void {
    const current = this.currentUserSubject.value;
    if (!current) return;
    const updated = { ...current, ...parcial };
    localStorage.setItem(this.USER_KEY, JSON.stringify(updated));
    this.currentUserSubject.next(updated);
  }

  private save(r: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, r.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(r));
    this.currentUserSubject.next(r);
  }

  private loadUser(): AuthResponse | null {
    const raw = localStorage.getItem(this.USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  isAdmin(): boolean {
    return this.getCurrentUser()?.rol === 'ADMIN';
  }
}