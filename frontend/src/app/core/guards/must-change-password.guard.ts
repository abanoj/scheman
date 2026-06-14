import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TokenService } from '../auth/token.service';

export const mustChangePasswordGuard: CanActivateFn = () => {
  const tokenService = inject(TokenService);
  const router = inject(Router);
  const user = tokenService.currentUser();

  if (user?.mustChangePassword) {
    return router.createUrlTree(['/change-password']);
  }
  return true;
};
