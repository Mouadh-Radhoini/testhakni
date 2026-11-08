import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

function withToken(req: HttpRequest<unknown>, token: string | null): HttpRequest<unknown> {
  return token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
}

/**
 * Intercepteur JWT. FE-03
 * - ajoute "Authorization: Bearer <accessToken>" aux appels vers notre API
 * - sur 401 : tente UNE fois de rafraichir le token puis rejoue la requete ;
 *   si le refresh echoue, deconnecte l'utilisateur.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);

  if (!req.url.startsWith(environment.apiUrl)) {
    return next(req);
  }
  const isAuthCall = req.url.startsWith(`${environment.apiUrl}/auth/`);

  return next(withToken(req, isAuthCall ? null : auth.accessToken)).pipe(
    catchError((err: unknown) => {
      const canRefresh =
        err instanceof HttpErrorResponse && err.status === 401 && !isAuthCall && !!auth.refreshToken;
      if (!canRefresh) {
        return throwError(() => err);
      }
      return auth.refresh().pipe(
        switchMap((res) => next(withToken(req, res.accessToken))),
        catchError((refreshErr: unknown) => {
          auth.logout();
          return throwError(() => refreshErr);
        }),
      );
    }),
  );
};
