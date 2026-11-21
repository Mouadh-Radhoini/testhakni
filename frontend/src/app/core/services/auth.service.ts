import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { finalize, Observable, shareReplay, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthResponse, Role, ServiceCategory, User } from '../models';

const STORAGE_KEY = 'lsm.auth';

interface StoredAuth {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface RegisterCustomer {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string | null;
  city?: string | null;
}

export interface RegisterProvider extends RegisterCustomer {
  phone: string;
  city: string;
  businessName: string;
  categories: ServiceCategory[];
}

/**
 * Etat d'authentification (signals) + appels /api/auth. FE-02 / FE-03
 * Les tokens sont gardes dans localStorage pour survivre au rechargement de la page.
 * Le serveur fait tourner le refresh token a chaque usage (SEC-03) : un ancien token rejoue
 * ferme la session. On n'envoie donc jamais deux refresh en parallele, et les onglets
 * se synchronisent via l'evenement "storage".
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly api = `${environment.apiUrl}/auth`;

  private readonly state = signal<StoredAuth | null>(this.load());
  private refreshInFlight: Observable<AuthResponse> | null = null;

  constructor() {
    // Un autre onglet a renouvele ou ferme la session : on reprend son etat.
    if (typeof window !== 'undefined') {
      window.addEventListener('storage', (event) => {
        if (event.key === STORAGE_KEY) {
          this.state.set(this.load());
        }
      });
    }
  }

  readonly user = computed(() => this.state()?.user ?? null);
  readonly isLoggedIn = computed(() => this.state() !== null);
  readonly role = computed<Role | null>(() => this.state()?.user.role ?? null);

  get accessToken(): string | null {
    return this.state()?.accessToken ?? null;
  }

  get refreshToken(): string | null {
    return this.state()?.refreshToken ?? null;
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.api}/login`, { email, password })
      .pipe(tap((res) => this.save(res)));
  }

  registerCustomer(body: RegisterCustomer): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.api}/register/customer`, body)
      .pipe(tap((res) => this.save(res)));
  }

  registerProvider(body: RegisterProvider): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.api}/register/provider`, body)
      .pipe(tap((res) => this.save(res)));
  }

  /**
   * Echange le refresh token contre un nouveau couple de tokens.
   * Les appels simultanes partagent la meme requete (le refresh token est a usage unique).
   */
  refresh(): Observable<AuthResponse> {
    if (!this.refreshInFlight) {
      this.refreshInFlight = this.http
        .post<AuthResponse>(`${this.api}/refresh`, { refreshToken: this.refreshToken })
        .pipe(
          tap((res) => this.save(res)),
          finalize(() => (this.refreshInFlight = null)),
          shareReplay(1),
        );
    }
    return this.refreshInFlight;
  }

  /** Met a jour l'utilisateur stocke (apres modification du profil). */
  updateUser(user: User): void {
    const current = this.state();
    if (current) {
      this.persist({ ...current, user });
    }
  }

  /** Ferme la session localement et revoque le refresh token cote serveur (au mieux, sans bloquer). */
  logout(redirect = true): void {
    const refreshToken = this.refreshToken;
    if (refreshToken) {
      this.http.post<void>(`${this.api}/logout`, { refreshToken }).subscribe({ error: () => undefined });
    }
    this.state.set(null);
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch {
      /* stockage indisponible */
    }
    if (redirect) {
      this.router.navigateByUrl('/login');
    }
  }

  hasRole(...roles: Role[]): boolean {
    const r = this.role();
    return r !== null && roles.includes(r);
  }

  private save(res: AuthResponse): void {
    this.persist({ accessToken: res.accessToken, refreshToken: res.refreshToken, user: res.user });
  }

  private persist(auth: StoredAuth): void {
    this.state.set(auth);
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
    } catch {
      /* stockage indisponible : la session reste en memoire */
    }
  }

  private load(): StoredAuth | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as StoredAuth) : null;
    } catch {
      return null;
    }
  }
}
