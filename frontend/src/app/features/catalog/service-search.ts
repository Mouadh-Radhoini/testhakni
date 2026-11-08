import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { catchError, of, switchMap, tap } from 'rxjs';

import { errorMessage } from '../../core/api-error';
import { Page, Service, ServiceCategory, ServiceSearch, SortOption } from '../../core/models';
import { CatalogService } from '../../core/services/catalog.service';
import { ServiceCard } from '../../shared/service-card';

const PAGE_SIZE = 12;

/**
 * Recherche de services (SEARCH-01 / SEARCH-02 cote UI). FE-06
 * Les filtres sont dans l'URL (?q=&category=...) : la page est partageable et
 * le bouton "precedent" du navigateur fonctionne.
 */
@Component({
  selector: 'app-service-search',
  imports: [ReactiveFormsModule, ServiceCard],
  template: `
    <section class="container">
      <h1>Trouver un service</h1>

      <form class="card filters" [formGroup]="form" (ngSubmit)="apply()">
        <div class="field grow">
          <label for="q">Mot-clé</label>
          <input id="q" formControlName="q" placeholder="fuite, ménage, peinture…" />
        </div>
        <div class="field">
          <label for="category">Catégorie</label>
          <select id="category" formControlName="category">
            <option value="">Toutes</option>
            @for (c of categories(); track c.code) {
              <option [value]="c.code">{{ c.label }}</option>
            }
          </select>
        </div>
        <div class="field">
          <label for="city">Ville</label>
          <input id="city" formControlName="city" />
        </div>
        <div class="field price">
          <label for="minPrice">Prix min</label>
          <input id="minPrice" type="number" min="0" formControlName="minPrice" />
        </div>
        <div class="field price">
          <label for="maxPrice">Prix max</label>
          <input id="maxPrice" type="number" min="0" formControlName="maxPrice" />
        </div>
        <div class="field">
          <label for="sort">Trier par</label>
          <select id="sort" formControlName="sort">
            <option value="NEWEST">Plus récents</option>
            <option value="PRICE_ASC">Prix croissant</option>
            <option value="PRICE_DESC">Prix décroissant</option>
          </select>
        </div>
        <div class="actions row">
          <button class="btn" type="submit">Rechercher</button>
          <button class="btn secondary" type="button" (click)="reset()">Effacer</button>
        </div>
      </form>

      @if (error()) {
        <div class="alert error mt" role="alert">{{ error() }}</div>
      } @else if (loading()) {
        <p class="muted mt">Recherche en cours…</p>
      } @else if (result(); as r) {
        <p class="muted mt">{{ r.totalElements }} service(s) trouvé(s)</p>
        @if (r.content.length === 0) {
          <div class="card empty">Aucun service ne correspond à votre recherche. Essayez d'élargir les filtres.</div>
        } @else {
          <div class="grid">
            @for (s of r.content; track s.id) {
              <app-service-card [service]="s" />
            }
          </div>
          @if (r.totalPages > 1) {
            <nav class="row pager" aria-label="Pagination">
              <button class="btn secondary small" [disabled]="r.first" (click)="goTo(r.page - 1)">← Précédent</button>
              <span class="muted small">Page {{ r.page + 1 }} / {{ r.totalPages }}</span>
              <button class="btn secondary small" [disabled]="r.last" (click)="goTo(r.page + 1)">Suivant →</button>
            </nav>
          }
        }
      }
    </section>
  `,
  styles: `
    .filters { display: flex; flex-wrap: wrap; gap: 0.75rem; align-items: flex-end; }
    .filters .field { min-width: 140px; }
    .filters .grow { flex: 1 1 200px; }
    .filters .price { max-width: 110px; min-width: 90px; }
    .pager { justify-content: center; margin-top: 1.5rem; }
  `,
})
export class ServiceSearchPage {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly catalog = inject(CatalogService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly categories = toSignal(this.catalog.categories(), { initialValue: [] });
  protected readonly result = signal<Page<Service> | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.group({
    q: [''],
    category: this.fb.control<ServiceCategory | ''>(''),
    city: [''],
    minPrice: this.fb.control<number | null>(null),
    maxPrice: this.fb.control<number | null>(null),
    sort: this.fb.control<SortOption>('NEWEST'),
  });

  constructor() {
    // A chaque changement d'URL : remplir le formulaire et relancer la recherche
    this.route.queryParamMap
      .pipe(
        tap((params) => this.form.setValue(this.toFormValue(params), { emitEvent: false })),
        tap(() => {
          this.loading.set(true);
          this.error.set(null);
        }),
        switchMap((params) =>
          this.catalog.search(this.toCriteria(params)).pipe(
            catchError((e) => {
              this.error.set(errorMessage(e));
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((page) => {
        this.result.set(page);
        this.loading.set(false);
      });
  }

  apply(): void {
    this.navigate({ ...this.form.getRawValue(), page: 0 });
  }

  reset(): void {
    this.router.navigate([], { queryParams: {} });
  }

  goTo(page: number): void {
    this.navigate({ ...this.form.getRawValue(), page });
    window.scrollTo({ top: 0 });
  }

  private navigate(values: ServiceSearch): void {
    const queryParams: Record<string, string | number> = {};
    for (const [k, v] of Object.entries(values)) {
      if (v !== null && v !== undefined && v !== '' && !(k === 'page' && v === 0) && !(k === 'sort' && v === 'NEWEST')) {
        queryParams[k] = v as string | number;
      }
    }
    this.router.navigate([], { queryParams });
  }

  private toFormValue(p: ParamMap) {
    const num = (k: string) => (p.get(k) ? Number(p.get(k)) : null);
    return {
      q: p.get('q') ?? '',
      category: (p.get('category') ?? '') as ServiceCategory | '',
      city: p.get('city') ?? '',
      minPrice: num('minPrice'),
      maxPrice: num('maxPrice'),
      sort: (p.get('sort') ?? 'NEWEST') as SortOption,
    };
  }

  private toCriteria(p: ParamMap): ServiceSearch {
    return { ...this.toFormValue(p), page: Number(p.get('page') ?? 0), size: PAGE_SIZE };
  }
}
