import { ReviewsPanel } from '../marketplace/reviews';
import { DatePipe } from '@angular/common';
import { Component, inject, input, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { errorMessage } from '../../core/api-error';
import { PRICE_TYPE_LABELS, Service } from '../../core/models';
import { AuthService } from '../../core/services/auth.service';
import { CatalogService } from '../../core/services/catalog.service';
import { PricePipe } from '../../shared/price.pipe';

/** Detail d'un service + bouton "Faire une demande" (SEARCH-03). FE-06 */
@Component({
  selector: 'app-service-detail',
  imports: [ReviewsPanel, RouterLink, PricePipe, DatePipe],
  template: `
    <section class="container">
      <p><a routerLink="/services">← Retour aux résultats</a></p>

      @if (error()) {
        <div class="card empty">
          <p>{{ error() }}</p>
          <a class="btn" routerLink="/services">Voir les services</a>
        </div>
      } @else if (service(); as s) {
        <div class="layout">
          <article class="card">
            <div class="row">
              <span class="badge">{{ s.categoryLabel }}</span>
              @if (s.status !== 'ACTIVE') { <span class="badge" [class]="s.status">{{ s.status }}</span> }
            </div>
            <h1 class="mt">{{ s.title }}</h1>
            <p class="muted">📍 {{ s.city }} · publié le {{ s.createdAt | date: 'dd/MM/yyyy' }}</p>
            <p class="description">{{ s.description }}</p>
          </article>

          <aside class="card side">
            <p class="big price">{{ s.price | price: s.priceType }}</p>
            <p class="muted small">{{ priceTypeLabel[s.priceType] }}</p>

            @if (auth.role() === 'CUSTOMER') {
              <a class="btn" routerLink="/requests/new"
                 [queryParams]="{ serviceId: s.id, category: s.category, city: s.city, title: s.title }">
                Faire une demande
              </a>
            } @else if (!auth.isLoggedIn()) {
              <a class="btn" routerLink="/login" [queryParams]="{ returnUrl: '/services/' + s.id }">
                Connectez-vous pour faire une demande
              </a>
            }

            @if (s.provider; as p) {
              <hr />
              <h2>{{ p.businessName }}</h2>
              <p class="small muted">
                📍 {{ p.city }}
                @if (p.yearsOfExperience) { · {{ p.yearsOfExperience }} ans d'expérience }
              </p>
              <p class="small">{{ p.available ? '🟢 Disponible' : '⚪ Indisponible pour le moment' }}</p>
              <a class="btn secondary" [routerLink]="['/providers', p.providerId]">Voir le profil du prestataire</a>
            }
          </aside>
        </div>
        <app-reviews group="services" [id]="s.id"/>
      } @else {
        <p class="muted">Chargement…</p>
      }
    </section>
  `,
  styles: `
    .layout { display: grid; gap: 1rem; grid-template-columns: 2fr 1fr; align-items: start; }
    @media (max-width: 800px) { .layout { grid-template-columns: 1fr; } }
    .description { white-space: pre-line; }
    .side { display: flex; flex-direction: column; gap: 0.6rem; }
    .side p { margin: 0; }
    .big { font-size: 1.6rem; }
    hr { border: 0; border-top: 1px solid var(--c-border); width: 100%; }
  `,
})
export class ServiceDetail implements OnInit {
  private readonly catalog = inject(CatalogService);
  protected readonly auth = inject(AuthService);

  readonly id = input.required<string>();

  protected readonly priceTypeLabel = PRICE_TYPE_LABELS;
  protected readonly service = signal<Service | null>(null);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.catalog.service(this.id()).subscribe({
      next: (s) => this.service.set(s),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
