import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

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
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly api = `${environment.apiUrl}/auth`;

  private readonly state = signal<StoredAuth | null>(this.load());

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

  /** Echange le refresh token contre un nouveau couple de tokens. */
  refresh(): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.api}/refresh`, { refreshToken: this.refreshToken })
      .pipe(tap((res) => this.save(res)));
  }

  /** Met a jour l'utilisateur stocke (apres modification du profil). */
  updateUser(user: User): void {
    const current = this.state();
    if (current) {
      this.persist({ ...current, user });
    }
  }

  logout(redirect = true): void {
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
