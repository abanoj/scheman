import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { TokenService } from '../auth/token.service';
import { Role } from '../models/auth.models';

export const roleGuard = (allowedRoles: Role[]): CanActivateFn =>
  (_route: ActivatedRouteSnapshot) => {
    const tokenService = inject(TokenService);
    const router = inject(Router);
    const user = tokenService.currentUser();

    if (user && allowedRoles.includes(user.role)) {
      return true;
    }
    return router.createUrlTree(['/dashboard']);
  };
