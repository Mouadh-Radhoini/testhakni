import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { User } from '../models';
import { AuthService } from './auth.service';

/** Compte de l'utilisateur connecte. AUTH-09 */
@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly api = `${environment.apiUrl}/users/me`;

  me(): Observable<User> {
    return this.http.get<User>(this.api);
  }

  update(body: Pick<User, 'firstName' | 'lastName' | 'phone' | 'city'>): Observable<User> {
    return this.http.put<User>(this.api, body).pipe(tap((u) => this.auth.updateUser(u)));
  }

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.http.put<void>(`${this.api}/password`, { currentPassword, newPassword });
  }
}
