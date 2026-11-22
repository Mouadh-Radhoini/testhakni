import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { environment } from '../../../environments/environment';
import { AuthResponse } from '../models';
import { AuthService } from './auth.service';

const RESPONSE: AuthResponse = {
  accessToken: 'access-1',
  refreshToken: 'refresh-1',
  tokenType: 'Bearer',
  expiresIn: 900,
  user: {
    id: 'u1',
    email: 'a@mail.com',
    firstName: 'Ali',
    lastName: 'B',
    phone: null,
    city: null,
    role: 'CUSTOMER',
    status: 'ACTIVE',
    createdAt: '',
  },
};

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('is logged out by default', () => {
    expect(service.isLoggedIn()).toBeFalse();
    expect(service.role()).toBeNull();
  });

  it('stores tokens and user after login', () => {
    service.login('a@mail.com', 'Secret123!').subscribe();
    const req = http.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.body).toEqual({ email: 'a@mail.com', password: 'Secret123!' });
    req.flush(RESPONSE);

    expect(service.isLoggedIn()).toBeTrue();
    expect(service.role()).toBe('CUSTOMER');
    expect(service.accessToken).toBe('access-1');
    expect(localStorage.getItem('lsm.auth')).toContain('refresh-1');
    expect(service.hasRole('CUSTOMER', 'ADMIN')).toBeTrue();
    expect(service.hasRole('PROVIDER')).toBeFalse();
  });

  it('sends the refresh token and replaces tokens', () => {
    service.login('a@mail.com', 'x').subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush(RESPONSE);

    service.refresh().subscribe();
    const req = http.expectOne(`${environment.apiUrl}/auth/refresh`);
    expect(req.request.body).toEqual({ refreshToken: 'refresh-1' });
    req.flush({ ...RESPONSE, accessToken: 'access-2' });
    expect(service.accessToken).toBe('access-2');
  });

  it('clears everything and redirects on logout', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl');
    service.login('a@mail.com', 'x').subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush(RESPONSE);

    service.logout();
    const revoke = http.expectOne(`${environment.apiUrl}/auth/logout`);
    expect(revoke.request.body).toEqual({ refreshToken: 'refresh-1' });
    revoke.flush(null, { status: 204, statusText: 'No Content' });
    expect(service.isLoggedIn()).toBeFalse();
    expect(localStorage.getItem('lsm.auth')).toBeNull();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
  });

  it('shares one refresh request between simultaneous callers', () => {
    service.login('a@mail.com', 'x').subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush(RESPONSE);

    const seen: string[] = [];
    service.refresh().subscribe((r) => seen.push(r.accessToken));
    service.refresh().subscribe((r) => seen.push(r.accessToken));
    http.expectOne(`${environment.apiUrl}/auth/refresh`).flush({ ...RESPONSE, accessToken: 'access-2' });
    expect(seen).toEqual(['access-2', 'access-2']);

    // Une fois termine, un nouveau refresh repart bien vers le serveur
    service.refresh().subscribe();
    http.expectOne(`${environment.apiUrl}/auth/refresh`).flush(RESPONSE);
  });

  it('logs out without calling the server when there is no session', () => {
    service.logout(false);
    http.expectNone(`${environment.apiUrl}/auth/logout`);
  });
});
