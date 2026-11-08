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

  it('shows the title', () => {
    const el = render(() => of({ status: 'UP' })).nativeElement as HTMLElement;
    expect(el.querySelector('h1')?.textContent).toContain('Local Services Marketplace');
  });

  it('shows API connected when backend is up', () => {
    const el = render(() => of({ status: 'UP' })).nativeElement as HTMLElement;
    expect(el.querySelector('.api')?.textContent).toContain('API connectée');
  });

  it('shows API unreachable on error', () => {
    const el = render(() => throwError(() => new Error('down'))).nativeElement as HTMLElement;
    expect(el.querySelector('.api')?.textContent).toContain('injoignable');
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
});
