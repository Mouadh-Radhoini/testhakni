import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { App } from './app';
import { AuthService } from './core/services/auth.service';

describe('App', () => {
  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('shows login link when logged out', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const nav = (fixture.nativeElement as HTMLElement).querySelector('nav')!;
    expect(nav.textContent).toContain('Connexion');
    expect(nav.textContent).not.toContain('Mes services');
  });

  it('shows provider menu when logged in as provider', () => {
    const auth = TestBed.inject(AuthService);
    (auth as unknown as { persist: (a: unknown) => void }).persist({
      accessToken: 'a',
      refreshToken: 'r',
      user: { id: 'u1', firstName: 'Karim', role: 'PROVIDER' },
    });
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const nav = (fixture.nativeElement as HTMLElement).querySelector('nav')!;
    expect(nav.textContent).toContain('Mes services');
    expect(nav.textContent).toContain('Déconnexion');
    expect(nav.textContent).not.toContain('Mes demandes');
  });
});
