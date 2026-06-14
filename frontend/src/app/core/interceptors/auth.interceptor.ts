import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError, BehaviorSubject, filter, take } from 'rxjs';
import { TokenService } from '../auth/token.service';
import { AuthService } from '../auth/auth.service';
import { Router } from '@angular/router';

let isRefreshing = false;
const refreshSubject = new BehaviorSubject<string | null>(null);

export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
) => {
  const tokenService = inject(TokenService);
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = tokenService.getAccessToken();
  const authedReq = token ? addToken(req, token) : req;

  return next(authedReq).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status !== 401 || req.url.includes('/auth/')) {
        return throwError(() => err);
      }

      const refreshToken = tokenService.getRefreshToken();
      if (!refreshToken) {
        tokenService.clear();
        router.navigate(['/login']);
        return throwError(() => err);
      }

      if (isRefreshing) {
        return refreshSubject.pipe(
          filter((t): t is string => t !== null),
          take(1),
          switchMap((newToken) => next(addToken(req, newToken)))
        );
      }

      isRefreshing = true;
      refreshSubject.next(null);

      return authService.refresh(refreshToken).pipe(
        switchMap((res) => {
          isRefreshing = false;
          refreshSubject.next(res.access_token);
          return next(addToken(req, res.access_token));
        }),
        catchError((refreshErr) => {
          isRefreshing = false;
          tokenService.clear();
          router.navigate(['/login']);
          return throwError(() => refreshErr);
        })
      );
    })
  );
};

function addToken(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
}
