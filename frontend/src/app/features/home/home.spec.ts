import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { Home } from './home';
import { HealthService } from '../../core/services/health.service';

describe('Home', () => {
  function render(check: () => unknown) {
    TestBed.configureTestingModule({
      imports: [Home],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: HealthService, useValue: { check } },
      ],
    });
    const fixture = TestBed.createComponent(Home);
    fixture.detectChanges();
    return fixture;
  }

  it('shows the headline and the search form', () => {
    const el = render(() => of({ status: 'UP' })).nativeElement as HTMLElement;
    expect(el.querySelector('h1')?.textContent).toContain('Le bon professionnel');
    expect(el.querySelector('form[role="search"]')).not.toBeNull();
  });

  it('shows no availability warning when backend is up', () => {
    const el = render(() => of({ status: 'UP' })).nativeElement as HTMLElement;
    expect(el.querySelector('.api')).toBeNull();
  });

  it('warns the visitor when the backend is unreachable', () => {
    const el = render(() => throwError(() => new Error('down'))).nativeElement as HTMLElement;
    expect(el.querySelector('.api')?.textContent).toContain('indisponible');
  });

  it('navigates to the search page with the keyword', () => {
    const fixture = render(() => of({ status: 'UP' }));
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    const home = fixture.componentInstance as unknown as { query: string; search: () => void };
    home.query = '  fuite ';
    home.search();
    expect(router.navigate).toHaveBeenCalledWith(['/services'], { queryParams: { q: 'fuite' } });
  });

  it('adds the city to the search when given', () => {
    const fixture = render(() => of({ status: 'UP' }));
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    const home = fixture.componentInstance as unknown as { query: string; city: string; search: () => void };
    home.query = 'ménage';
    home.city = ' Sousse ';
    home.search();
    expect(router.navigate).toHaveBeenCalledWith(['/services'], { queryParams: { q: 'ménage', city: 'Sousse' } });
  });
});
