import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, provideRouter, Router, RouterStateSnapshot, UrlTree } from '@angular/router';

import { Role } from '../models';
import { AuthService } from '../services/auth.service';
import { authGuard, guestGuard, roleGuard } from './auth.guards';

describe('auth guards', () => {
  let loggedIn = false;
  let role: Role | null = null;

  const state = { url: '/provider/services' } as RouterStateSnapshot;
  const route = {} as ActivatedRouteSnapshot;

  beforeEach(() => {
    loggedIn = false;
    role = null;
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            isLoggedIn: () => loggedIn,
            hasRole: (...roles: Role[]) => role !== null && roles.includes(role),
          },
        },
      ],
    });
  });

  const run = (guard: typeof authGuard) => TestBed.runInInjectionContext(() => guard(route, state));
  const url = (r: unknown) => TestBed.inject(Router).serializeUrl(r as UrlTree);

  it('authGuard redirects anonymous users to login with returnUrl', () => {
    expect(url(run(authGuard))).toBe('/login?returnUrl=%2Fprovider%2Fservices');
    loggedIn = true;
    expect(run(authGuard)).toBeTrue();
  });

  it('roleGuard blocks the wrong role', () => {
    loggedIn = true;
    role = 'CUSTOMER';
    expect(url(run(roleGuard('PROVIDER')))).toBe('/forbidden');
    role = 'PROVIDER';
    expect(run(roleGuard('PROVIDER'))).toBeTrue();
  });

  it('guestGuard sends logged-in users home', () => {
    expect(run(guestGuard)).toBeTrue();
    loggedIn = true;
    expect(url(run(guestGuard))).toBe('/');
  });
});
