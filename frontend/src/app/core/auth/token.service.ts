import { Injectable, signal } from '@angular/core';
import { CurrentUser, JwtPayload } from '../models/auth.models';

const REFRESH_TOKEN_KEY = 'scheman.refreshToken';

/**
 * Token storage strategy:
 * - The short-lived ACCESS token (15 min) is kept in memory only, so it is
 *   never exposed to XSS via persistent storage.
 * - The ROTATING REFRESH token is persisted in localStorage so the session
 *   survives a page reload. It is rotated on every use and revocable
 *   server-side, which limits the impact of theft. On app start a silent
 *   refresh exchanges it for a fresh access token (see session-init.ts).
 */
@Injectable({ providedIn: 'root' })
export class TokenService {
  private accessToken: string | null = null;
  private refreshToken: string | null = this.readStoredRefreshToken();
  private readonly _currentUser = signal<CurrentUser | null>(null);

  readonly currentUser = this._currentUser.asReadonly();

  setTokens(accessToken: string, refreshToken: string, mustChangePassword: boolean): void {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.persistRefreshToken(refreshToken);

    const payload = this.decodePayload(accessToken);
    if (payload) {
      this._currentUser.set({
        id: payload.id,
        email: payload.sub,
        role: payload.role,
        mustChangePassword,
      });
    }
  }

  getAccessToken(): string | null {
    return this.accessToken;
  }

  getRefreshToken(): string | null {
    return this.refreshToken;
  }

  /** True when a refresh token survives in storage (e.g. after a reload). */
  hasPersistedSession(): boolean {
    return !!this.refreshToken;
  }

  clear(): void {
    this.accessToken = null;
    this.refreshToken = null;
    this._currentUser.set(null);
    this.removeStoredRefreshToken();
  }

  isAuthenticated(): boolean {
    const token = this.accessToken;
    if (!token) return false;
    const payload = this.decodePayload(token);
    return !!payload && payload.exp * 1000 > Date.now();
  }

  private decodePayload(token: string): JwtPayload | null {
    try {
      const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(atob(base64)) as JwtPayload;
    } catch {
      return null;
    }
  }

  private readStoredRefreshToken(): string | null {
    try {
      return localStorage.getItem(REFRESH_TOKEN_KEY);
    } catch {
      return null;
    }
  }

  private persistRefreshToken(token: string): void {
    try {
      localStorage.setItem(REFRESH_TOKEN_KEY, token);
    } catch {
      /* storage unavailable (private mode / quota) — session stays in-memory only */
    }
  }

  private removeStoredRefreshToken(): void {
    try {
      localStorage.removeItem(REFRESH_TOKEN_KEY);
    } catch {
      /* ignore */
    }
  }
}
