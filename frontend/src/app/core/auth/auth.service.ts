import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TokenService } from './token.service';
import {
  LoginRequest,
  AuthResponse,
  ChangePasswordRequest,
  ManagerCreateRequest,
  ManagerResponse,
} from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenService = inject(TokenService);
  private readonly router = inject(Router);
  private readonly base = `${environment.apiUrl}/auth`;

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/login`, request).pipe(
      tap((res) => {
        this.tokenService.setTokens(res.access_token, res.refresh_token, res.mustChangePassword);
        if (res.mustChangePassword) {
          this.router.navigate(['/change-password']);
        } else {
          this.router.navigate(['/dashboard']);
        }
      })
    );
  }

  refresh(refreshToken: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.base}/refresh`, { refreshToken })
      .pipe(
        tap((res) => {
          this.tokenService.setTokens(
            res.access_token,
            res.refresh_token,
            res.mustChangePassword
          );
        })
      );
  }

  logout(): void {
    const refreshToken = this.tokenService.getRefreshToken();
    if (refreshToken) {
      this.http.post(`${this.base}/logout`, { refreshToken }).subscribe({
        complete: () => this.clearAndRedirect(),
        error: () => this.clearAndRedirect(),
      });
    } else {
      this.clearAndRedirect();
    }
  }

  changePassword(userId: string, request: ChangePasswordRequest): Observable<void> {
    return this.http.patch<void>(`${this.base}/${userId}/password`, request).pipe(
      tap(() => {
        const user = this.tokenService.currentUser();
        if (user) {
          this.tokenService.setTokens(
            this.tokenService.getAccessToken()!,
            this.tokenService.getRefreshToken()!,
            false
          );
        }
      })
    );
  }

  createManager(request: ManagerCreateRequest): Observable<ManagerResponse> {
    return this.http.post<ManagerResponse>(`${this.base}/signup/manager`, request);
  }

  private clearAndRedirect(): void {
    this.tokenService.clear();
    this.router.navigate(['/login']);
  }
}
