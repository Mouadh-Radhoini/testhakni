import { Component, inject, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { catchError, map, of } from 'rxjs';

import { Service } from '../../core/models';
import { AuthService } from '../../core/services/auth.service';
import { CatalogService } from '../../core/services/catalog.service';
import { HealthService } from '../../core/services/health.service';
import { CATEGORY_ICONS, Icon } from '../../shared/icon';
import { ServiceCard } from '../../shared/service-card';
import { TranslatePipe } from '../../core/i18n/i18n';

type ApiState = 'loading' | 'up' | 'down';

/** Page d'accueil : recherche, categories, fonctionnement, derniers services, appel aux prestataires. */
@Component({
  selector: 'app-home',
  imports: [FormsModule, RouterLink, Icon, ServiceCard, TranslatePipe],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home implements OnInit {
  private readonly healthService = inject(HealthService);
  private readonly catalog = inject(CatalogService);
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);

  protected readonly icons = CATEGORY_ICONS;
  protected readonly apiState = signal<ApiState>('loading');
  protected readonly categories = toSignal(this.catalog.categories().pipe(catchError(() => of([]))), {
    initialValue: [],
  });
  protected readonly latest = toSignal(
    this.catalog.search({ sort: 'NEWEST', size: 6 }).pipe(
      map((page) => page.content),
      catchError(() => of([] as Service[])),
    ),
    { initialValue: [] as Service[] },
  );

  protected query = '';
  protected city = '';

  protected readonly steps = [
    { icon: 'chat', title: 'home.step1Title', text: 'home.step1' },
    { icon: 'inbox', title: 'home.step2Title', text: 'home.step2' },
    { icon: 'star', title: 'home.step3Title', text: 'home.step3' },
  ] as const;

  ngOnInit(): void {
    this.healthService.check().subscribe({
      next: (res) => this.apiState.set(res.status === 'UP' ? 'up' : 'down'),
      error: () => this.apiState.set('down'),
    });
  }

  search(): void {
    const q = this.query.trim();
    const city = this.city.trim();
    const queryParams: Record<string, string> = {};
    if (q) queryParams['q'] = q;
    if (city) queryParams['city'] = city;
    this.router.navigate(['/services'], { queryParams });
  }
}
