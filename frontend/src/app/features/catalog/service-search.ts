import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router, RouterLink } from '@angular/router';
import { catchError, of, switchMap, tap } from 'rxjs';

import { errorMessage } from '../../core/api-error';
import { Page, Service, ServiceCategory, ServiceSearch, SortOption } from '../../core/models';
import { AuthService } from '../../core/services/auth.service';
import { CatalogService } from '../../core/services/catalog.service';
import { ServiceCard } from '../../shared/service-card';
import { TranslatePipe } from '../../core/i18n/i18n';

const PAGE_SIZE = 12;

/**
 * Recherche de services (SEARCH-01 / SEARCH-02 cote UI). FE-06
 * Les filtres sont dans l'URL (?q=&category=...) : la page est partageable et
 * le bouton "precedent" du navigateur fonctionne.
 */
@Component({
  selector: 'app-service-search',
  imports: [ReactiveFormsModule, ServiceCard, RouterLink, TranslatePipe],
  template: `
    <section class="container">
      <h1>{{ 'search.title' | t }}</h1>

      <form class="card filters" [formGroup]="form" (ngSubmit)="apply()">
        <div class="field grow">
          <label for="q">{{ 'search.keyword' | t }}</label>
          <input id="q" formControlName="q" [placeholder]="'search.keywordPh' | t" />
        </div>
        <div class="field">
          <label for="category">{{ 'search.category' | t }}</label>
          <select id="category" formControlName="category">
            <option value="">{{ 'search.all' | t }}</option>
            @for (c of categories(); track c.code) {
              <option [value]="c.code">{{ ('cat.' + c.code) | t }}</option>
            }
          </select>
        </div>
        <div class="field">
          <label for="city">{{ 'search.city' | t }}</label>
          <input id="city" formControlName="city" />
        </div>
        @if (auth.isLoggedIn()) {
          <div class="field price">
            <label for="minPrice">{{ 'search.minPrice' | t }}</label>
            <input id="minPrice" type="number" min="0" formControlName="minPrice" />
          </div>
          <div class="field price">
            <label for="maxPrice">{{ 'search.maxPrice' | t }}</label>
            <input id="maxPrice" type="number" min="0" formControlName="maxPrice" />
          </div>
          <div class="field">
            <label for="sort">{{ 'search.sortBy' | t }}</label>
            <select id="sort" formControlName="sort">
              <option value="NEWEST">{{ 'search.newest' | t }}</option>
              <option value="PRICE_ASC">{{ 'search.priceAsc' | t }}</option>
              <option value="PRICE_DESC">{{ 'search.priceDesc' | t }}</option>
            </select>
          </div>
        }
        <div class="actions row">
          <button class="btn" type="submit">{{ 'search.submit' | t }}</button>
          <button class="btn secondary" type="button" (click)="reset()">{{ 'search.clear' | t }}</button>
        </div>
      </form>

      @if (!auth.isLoggedIn()) {
        <div class="alert info mt" role="note">
          {{ 'search.membersOnly' | t }}
          <a routerLink="/register">{{ 'search.createAccount' | t }}</a> · <a routerLink="/login">{{ 'nav.login' | t }}</a>
        </div>
      }

      @if (error()) {
        <div class="alert error mt" role="alert">{{ error() }}</div>
      } @else if (loading()) {
        <p class="muted mt">{{ 'search.loading' | t }}</p>
      } @else if (result(); as r) {
        <p class="muted mt">{{ 'search.count' | t: { n: r.totalElements } }}</p>
        @if (r.content.length === 0) {
          <div class="card empty">{{ 'search.none' | t }}</div>
        } @else {
          <div class="grid">
            @for (s of r.content; track s.id) {
              <app-service-card [service]="s" />
            }
          </div>
          @if (r.totalPages > 1) {
            <nav class="row pager" [attr.aria-label]="'search.pagination' | t">
              <button class="btn secondary small" [disabled]="r.first" (click)="goTo(r.page - 1)"><span class="flip">←</span> {{ 'search.prev' | t }}</button>
              <span class="muted small">{{ 'search.page' | t: { p: r.page + 1, total: r.totalPages } }}</span>
              <button class="btn secondary small" [disabled]="r.last" (click)="goTo(r.page + 1)">{{ 'search.next' | t }} <span class="flip">→</span></button>
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
  protected readonly auth = inject(AuthService);

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
      // Les filtres de prix exigent un compte (le serveur repond 401 sinon) : ignores pour un visiteur
      minPrice: this.auth.isLoggedIn() ? num('minPrice') : null,
      maxPrice: this.auth.isLoggedIn() ? num('maxPrice') : null,
      sort: (this.auth.isLoggedIn() ? (p.get('sort') ?? 'NEWEST') : 'NEWEST') as SortOption,
    };
  }

  private toCriteria(p: ParamMap): ServiceSearch {
    return { ...this.toFormValue(p), page: Number(p.get('page') ?? 0), size: PAGE_SIZE };
  }
}
