import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isAdmin()) return true;

  // B11 fix: a logged-in non-admin user should go to dashboard, not login
  if (auth.getCurrentSession()) {
    return router.createUrlTree(['/dashboard']);
  }
  return router.createUrlTree(['/auth/login']);
};
