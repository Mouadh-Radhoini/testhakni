import { HttpClient } from '@angular/common/http';
import { effect, inject, Injectable, signal } from '@angular/core';
import { catchError, EMPTY, Observable, tap, timer } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Role } from '../models';
import { AuthService } from './auth.service';

export type ContactStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED';
export type ContactStateName = 'NONE' | 'PENDING_OUT' | 'PENDING_IN' | 'ACCEPTED' | 'DECLINED';

export interface Contact {
  id: string;
  otherId: string;
  otherName: string;
  otherAvatarUrl: string | null;
  otherRole: Role | null;
  status: ContactStatus;
  incoming: boolean;
  createdAt: string;
}

export interface ContactState {
  contactId: string | null;
  state: ContactStateName;
}

/** Contacts client / prestataire : demandes, acceptation, et compteur des demandes recues. */
@Injectable({ providedIn: 'root' })
export class ContactService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly api = `${environment.apiUrl}/contacts`;

  /** Demandes recues en attente (pastille dans le menu du compte). */
  readonly pending = signal(0);

  constructor() {
    effect((cleanup) => {
      const user = this.auth.user();
      this.pending.set(0);
      if (!user || (user.role !== 'CUSTOMER' && user.role !== 'PROVIDER')) return;
      const poll = timer(0, 60_000).subscribe(() => this.refreshPending());
      cleanup(() => poll.unsubscribe());
    });
  }

  list(): Observable<Contact[]> {
    return this.http.get<Contact[]>(this.api);
  }

  stateWith(userId: string): Observable<ContactState> {
    return this.http.get<ContactState>(`${this.api}/with/${encodeURIComponent(userId)}`);
  }

  request(targetId: string): Observable<Contact> {
    return this.http.post<Contact>(this.api, { targetId });
  }

  accept(id: string): Observable<Contact> {
    return this.http.post<Contact>(`${this.api}/${encodeURIComponent(id)}/accept`, null).pipe(tap(() => this.refreshPending()));
  }

  decline(id: string): Observable<Contact> {
    return this.http.post<Contact>(`${this.api}/${encodeURIComponent(id)}/decline`, null).pipe(tap(() => this.refreshPending()));
  }

  remove(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${encodeURIComponent(id)}`);
  }

  refreshPending(): void {
    this.http
      .get<{ incoming: number }>(`${this.api}/pending`)
      .pipe(catchError(() => EMPTY))
      .subscribe((r) => this.pending.set(r.incoming));
  }
}
