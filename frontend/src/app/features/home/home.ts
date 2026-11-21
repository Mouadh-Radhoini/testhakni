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

type ApiState = 'loading' | 'up' | 'down';

/** Page d'accueil : recherche, categories, fonctionnement, derniers services, appel aux prestataires. */
@Component({
  selector: 'app-home',
  imports: [FormsModule, RouterLink, Icon, ServiceCard],
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
    { icon: 'chat', title: 'Décrivez votre besoin', text: 'Publiez une demande en deux minutes : quoi, où et quand.' },
    { icon: 'inbox', title: 'Comparez les offres', text: 'Les prestataires de votre ville vous envoient leur prix et leur date.' },
    { icon: 'star', title: 'Réservez et notez', text: "Suivez l'intervention, confirmez la fin, puis laissez votre avis." },
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
