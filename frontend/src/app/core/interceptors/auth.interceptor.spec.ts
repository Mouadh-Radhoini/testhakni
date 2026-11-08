import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthResponse } from '../models';
import { AuthService } from '../services/auth.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  let auth: jasmine.SpyObj<AuthService> & { accessToken: string | null; refreshToken: string | null };

  beforeEach(() => {
    auth = Object.assign(jasmine.createSpyObj<AuthService>('AuthService', ['refresh', 'logout']), {
      accessToken: 'old-token' as string | null,
      refreshToken: 'refresh' as string | null,
    });
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: auth },
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('adds the bearer token to API calls', () => {
    http.get(`${environment.apiUrl}/users/me`).subscribe();
    const req = backend.expectOne(`${environment.apiUrl}/users/me`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer old-token');
    req.flush({});
  });

  it('does not send the token to auth endpoints or other hosts', () => {
    http.post(`${environment.apiUrl}/auth/login`, {}).subscribe();
    http.get('https://example.org/data').subscribe();
    expect(backend.expectOne(`${environment.apiUrl}/auth/login`).request.headers.has('Authorization')).toBeFalse();
    expect(backend.expectOne('https://example.org/data').request.headers.has('Authorization')).toBeFalse();
    backend.match(() => true).forEach((r) => r.flush({}));
  });

  it('refreshes the token once on 401 and replays the request', () => {
    auth.refresh.and.returnValue(of({ accessToken: 'new-token' } as unknown as AuthResponse));
    let result: unknown;
    http.get(`${environment.apiUrl}/users/me`).subscribe((r) => (result = r));

    backend.expectOne(`${environment.apiUrl}/users/me`).flush({}, { status: 401, statusText: 'Unauthorized' });
    const retry = backend.expectOne(`${environment.apiUrl}/users/me`);
    expect(retry.request.headers.get('Authorization')).toBe('Bearer new-token');
    retry.flush({ ok: true });

    expect(auth.refresh).toHaveBeenCalledTimes(1);
    expect(result).toEqual({ ok: true });
  });

  it('logs out when the refresh fails', () => {
    auth.refresh.and.returnValue(throwError(() => new Error('expired')));
    let failed = false;
    http.get(`${environment.apiUrl}/users/me`).subscribe({ error: () => (failed = true) });

    backend.expectOne(`${environment.apiUrl}/users/me`).flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(auth.logout).toHaveBeenCalled();
    expect(failed).toBeTrue();
  });

  it('lets 403 errors through without refreshing', () => {
    let status = 0;
    http.get(`${environment.apiUrl}/services/mine`).subscribe({ error: (e) => (status = e.status) });
    backend.expectOne(`${environment.apiUrl}/services/mine`).flush({}, { status: 403, statusText: 'Forbidden' });
    expect(status).toBe(403);
    expect(auth.refresh).not.toHaveBeenCalled();
  });
});
