import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { AuthResponse } from '../../core/models';
import { AuthService } from '../../core/services/auth.service';
import { Login } from './login';

describe('Login page', () => {
  let auth: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    auth = jasmine.createSpyObj<AuthService>('AuthService', ['login']);
    TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }],
    });
  });

  function setup() {
    const fixture = TestBed.createComponent(Login);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    const type = (id: string, value: string) => {
      const input = el.querySelector<HTMLInputElement>(`#${id}`)!;
      input.value = value;
      input.dispatchEvent(new Event('input'));
    };
    const submit = () => {
      el.querySelector('form')!.dispatchEvent(new Event('submit'));
      fixture.detectChanges();
    };
    return { fixture, el, type, submit };
  }

  it('does not call the API when the form is invalid', () => {
    const { el, submit } = setup();
    submit();
    expect(auth.login).not.toHaveBeenCalled();
    expect(el.textContent).toContain('Champ obligatoire');
  });

  it('redirects a provider to his services after login', () => {
    auth.login.and.returnValue(of({ user: { role: 'PROVIDER' } } as unknown as AuthResponse));
    const router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl');
    const { type, submit } = setup();
    type('email', 'p@mail.com');
    type('password', 'Secret123!');
    submit();
    expect(auth.login).toHaveBeenCalledWith('p@mail.com', 'Secret123!');
    expect(router.navigateByUrl).toHaveBeenCalledWith('/provider/services');
  });

  it('shows the API error message', () => {
    auth.login.and.returnValue(
      throwError(
        () => new HttpErrorResponse({ status: 401, error: { message: 'Email ou mot de passe incorrect' } }),
      ),
    );
    const { el, type, submit } = setup();
    type('email', 'p@mail.com');
    type('password', 'mauvais');
    submit();
    expect(el.querySelector('.alert.error')?.textContent).toContain('Email ou mot de passe incorrect');
  });
});
