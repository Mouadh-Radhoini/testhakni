import { ReviewsPanel } from '../marketplace/reviews';
import { DatePipe } from '@angular/common';
import { Component, inject, input, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { errorMessage } from '../../core/api-error';
import { PRICE_TYPE_LABELS, Service } from '../../core/models';
import { AuthService } from '../../core/services/auth.service';
import { CatalogService } from '../../core/services/catalog.service';
import { PricePipe } from '../../shared/price.pipe';
import { CATEGORY_ICONS, Icon } from '../../shared/icon';

/** Detail d'un service + bouton "Faire une demande" (SEARCH-03). FE-06 */
@Component({
  selector: 'app-service-detail',
  imports: [ReviewsPanel, RouterLink, PricePipe, DatePipe, Icon],
  template: `
    <section class="container">
      <p class="back"><a routerLink="/services">← Retour aux résultats</a></p>

      @if (error()) {
        <div class="card empty">
          <p>{{ error() }}</p>
          <a class="btn" routerLink="/services">Voir les services</a>
        </div>
      } @else if (service(); as s) {
        <div class="layout">
          <article class="card">
            <div class="row">
              <span class="icon-tile"><app-icon [name]="icons[s.category]" [size]="22" /></span>
              <span class="badge">{{ s.categoryLabel }}</span>
              @if (s.status !== 'ACTIVE') { <span class="badge" [class]="s.status">{{ s.status }}</span> }
            </div>
            <h1 class="mt">{{ s.title }}</h1>
            <p class="muted meta"><app-icon name="pin" [size]="16" /> {{ s.city }} · publié le {{ s.createdAt | date: 'dd/MM/yyyy' }}</p>
            @if (s.locked) {
              <div class="locked">
                <span class="icon-tile accent"><app-icon name="shield" [size]="22" /></span>
                <div>
                  <h2>Détails réservés aux membres</h2>
                  <p class="muted">La description, le prix et le prestataire sont visibles une fois connecté. L'inscription est gratuite et prend deux minutes.</p>
                  <div class="row">
                    <a class="btn" routerLink="/register">Créer un compte gratuit</a>
                    <a class="btn secondary" routerLink="/login" [queryParams]="{ returnUrl: '/services/' + s.id }">Se connecter</a>
                  </div>
                </div>
              </div>
            } @else {
              <p class="description">{{ s.description }}</p>
            }
          </article>

          <aside class="card side">
            @if (s.locked || !s.priceType) {
              <p class="big price blurred" aria-hidden="true">000 DT</p>
              <p class="muted small">Prix visible après connexion</p>
            } @else {
              <p class="big price">{{ s.price | price: s.priceType! }}</p>
              <p class="muted small">{{ priceTypeLabel[s.priceType!] }}</p>
            }

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
              <hr class="divider" />
              <h2>{{ p.businessName }}</h2>
              <p class="small muted meta">
                <app-icon name="pin" [size]="15" /> {{ p.city }}
                @if (p.yearsOfExperience) { · {{ p.yearsOfExperience }} ans d'expérience }
              </p>
              <p><span class="badge" [class.ACTIVE]="p.available" [class.INACTIVE]="!p.available">{{ p.available ? 'Disponible' : 'Indisponible pour le moment' }}</span></p>
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
    .big { font-size: 2rem; }
    .side { position: sticky; top: calc(var(--header-h) + 1rem); }
    .meta { display: flex; align-items: center; gap: 0.35rem; flex-wrap: wrap; }
    .back { margin-bottom: 1rem; }
    .layout .card:first-child { padding: 2rem; }
    .description { font-size: 1.02rem; line-height: 1.7; }
    .blurred { filter: blur(7px); user-select: none; }
    .locked { display: flex; gap: 1rem; align-items: flex-start; margin-top: 1rem; padding: 1.2rem;
      border-radius: var(--radius); background: var(--c-accent-soft); }
    .locked h2 { margin: 0 0 0.3rem; font-size: 1.1rem; }
  `,
})
export class ServiceDetail implements OnInit {
  protected readonly icons = CATEGORY_ICONS;
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
