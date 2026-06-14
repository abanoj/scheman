import { inject } from '@angular/core';
import { catchError, firstValueFrom, map, of } from 'rxjs';
import { TokenService } from './token.service';
import { AuthService } from './auth.service';

/**
 * Runs once before the app bootstraps. If a refresh token survived a page
 * reload, silently exchange it for a fresh access token so the route guards
 * see an authenticated session instead of bouncing the user to /login.
 *
 * Any failure (expired / revoked / rotated token) clears the stale token and
 * resolves normally, letting the guards redirect to the login page.
 */
export function restoreSession(): Promise<void> {
  const tokenService = inject(TokenService);
  const authService = inject(AuthService);

  const storedRefreshToken = tokenService.getRefreshToken();
  if (!storedRefreshToken) {
    return Promise.resolve();
  }

  return firstValueFrom(
    authService.refresh(storedRefreshToken).pipe(
      map(() => undefined),
      catchError(() => {
        tokenService.clear();
        return of(undefined);
      })
    )
  );
}
