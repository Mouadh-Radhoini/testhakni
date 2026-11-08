import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Role } from '../models';
import { AuthService } from '../services/auth.service';

/** Page reservee aux utilisateurs connectes ; sinon redirige vers /login?returnUrl=... FE-03 */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.isLoggedIn() ? true : router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

/** Page reservee a certains roles (ex. roleGuard('PROVIDER')). */
export function roleGuard(...roles: Role[]): CanActivateFn {
  return (_route, state) => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (!auth.isLoggedIn()) {
      return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
    }
    return auth.hasRole(...roles) ? true : router.createUrlTree(['/forbidden']);
  };
}

/** Login / inscription : inutile si deja connecte. */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.isLoggedIn() ? router.createUrlTree(['/']) : true;
};
