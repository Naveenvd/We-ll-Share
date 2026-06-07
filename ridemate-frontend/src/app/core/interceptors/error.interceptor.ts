import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject }                               from '@angular/core';
import { Router }                               from '@angular/router';
import { MatSnackBar }                          from '@angular/material/snack-bar';
import { catchError, throwError }               from 'rxjs';
import { AuthService }                          from '../services/auth.service';

/**
 * Global HTTP error interceptor.
 *
 * Handles:
 *  401 — clears session, redirects to /auth/login?expired=true
 *  403 — shows "Access denied" snackbar
 *  0   — network error / server unreachable
 *  5xx — generic server error snackbar
 *
 * All errors are re-thrown so individual components can still react
 * to 400/409/404 with specific messages.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router  = inject(Router);
  const auth    = inject(AuthService);
  const snack   = inject(MatSnackBar);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {

      switch (true) {
        case err.status === 401:
          auth.logout();
          router.navigate(['/auth/login'], { queryParams: { expired: true } });
          break;

        case err.status === 403:
          snack.open('Access denied. You are not authorised to perform this action.',
            'Dismiss', { duration: 5000, panelClass: 'snack-warn' });
          break;

        case err.status === 0:
          snack.open('Cannot reach the server. Please check your connection and try again.',
            'Dismiss', { duration: 6000, panelClass: 'snack-error' });
          break;

        case err.status >= 500:
          snack.open('A server error occurred. Please try again later.',
            'Dismiss', { duration: 5000, panelClass: 'snack-error' });
          break;
      }

      // Always re-throw so components handle 400/404/409 themselves
      return throwError(() => err);
    })
  );
};
