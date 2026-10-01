import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, UserResponse } from './auth.model';

const REFRESH_TOKEN_KEY = 'le_paysan_refresh_token';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;

  private readonly accessTokenSignal = signal<string | null>(null);
  readonly currentUser = signal<UserResponse | null>(null);
  readonly isAuthenticated = computed(() => this.accessTokenSignal() !== null);

  get accessToken(): string | null {
    return this.accessTokenSignal();
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, request).pipe(tap((res) => this.setSession(res)));
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, request).pipe(tap((res) => this.setSession(res)));
  }

  /** Tente de restaurer la session depuis le refresh token stocke (ex. au rechargement de page). */
  tryRestoreSession(): Observable<AuthResponse> | null {
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
    if (!refreshToken) {
      return null;
    }
    return this.refresh(refreshToken);
  }

  refresh(refreshToken: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/refresh`, { refreshToken })
      .pipe(tap((res) => this.setSession(res)));
  }

  forgotPassword(email: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/reset-password`, { token, newPassword });
  }

  updateProfile(request: {
    firstName: string;
    lastName: string;
    email?: string;
    phone?: string;
  }): Observable<UserResponse> {
    return this.http
      .put<UserResponse>(`${environment.apiBaseUrl}/users/me`, request)
      .pipe(tap((user) => this.currentUser.set(user)));
  }

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.http.put<void>(`${environment.apiBaseUrl}/users/me/password`, { currentPassword, newPassword });
  }

  uploadSecuritySelfie(selfie: File): Observable<UserResponse> {
    const formData = new FormData();
    formData.append('selfie', selfie);
    return this.http
      .put<UserResponse>(`${environment.apiBaseUrl}/users/me/security-selfie`, formData)
      .pipe(tap((user) => this.currentUser.set(user)));
  }

  accountRecoveryRequest(username: string, selfie: File): Observable<unknown> {
    const formData = new FormData();
    formData.append('username', username);
    formData.append('selfie', selfie);
    return this.http.post(`${this.baseUrl}/account-recovery-requests`, formData);
  }

  logout(): void {
    this.accessTokenSignal.set(null);
    this.currentUser.set(null);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    this.http.post(`${this.baseUrl}/logout`, {}).subscribe({ error: () => void 0 });
  }

  hasRole(role: string): boolean {
    return this.currentUser()?.roles.includes(role) ?? false;
  }

  private setSession(res: AuthResponse): void {
    this.accessTokenSignal.set(res.accessToken);
    this.currentUser.set(res.user);
    localStorage.setItem(REFRESH_TOKEN_KEY, res.refreshToken);
  }
}
