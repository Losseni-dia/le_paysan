import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }
  return router.createUrlTree(['/connexion']);
};

/** Usage : { path: '...', canActivate: [roleGuard('ROLE_ADMIN')] } */
export function roleGuard(role: string): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      return router.createUrlTree(['/connexion']);
    }
    if (!authService.hasRole(role)) {
      return router.createUrlTree(['/']);
    }
    return true;
  };
}
