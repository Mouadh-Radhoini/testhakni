import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Service } from '../core/models';
import { CATEGORY_ICONS, Icon } from './icon';
import { PricePipe } from './price.pipe';
import { TranslatePipe } from '../core/i18n/i18n';

/** Carte d'un service dans les listes (accueil, recherche, profil prestataire). */
@Component({
  selector: 'app-service-card',
  imports: [RouterLink, PricePipe, Icon, TranslatePipe],
  template: `
    <a class="service" [routerLink]="['/services', service().id]">
      <div class="top">
        <span class="icon-tile"><app-icon [name]="icon()" [size]="22" /></span>
        <span class="badge">{{ ('cat.' + service().category) | t }}</span>
        @if (service().status !== 'ACTIVE') {
          <span class="badge" [class]="service().status">{{ (service().status === 'INACTIVE' ? 'card.hidden' : 'card.suspended') | t }}</span>
        }
      </div>
      <h3>{{ service().title }}</h3>
      @if (service().locked) {
        <p class="desc locked-text">{{ 'card.locked' | t }}</p>
      } @else {
        <p class="desc">{{ service().description }}</p>
      }
      <div class="meta">
        <span><app-icon name="pin" [size]="15" /> {{ service().city }}</span>
        @if (service().provider; as p) {
          <span class="pro"><app-icon name="user" [size]="15" /> {{ p.businessName }}</span>
        }
      </div>
      <div class="foot">
        @if (service().locked || !service().priceType) {
          <span class="lock"><app-icon name="shield" [size]="15" /> {{ 'card.priceLocked' | t }}</span>
        } @else {
          <span class="price">{{ service().price | price: service().priceType! }}</span>
        }
        <span class="go">{{ 'card.view' | t }} <app-icon name="arrow" [size]="16" class="flip" /></span>
      </div>
    </a>
  `,
  styles: `
    :host { display: block; height: 100%; }
    .service {
      display: flex; flex-direction: column; gap: 0.55rem; height: 100%;
      padding: 1.2rem; background: var(--c-surface); border: 1px solid var(--c-border);
      border-radius: var(--radius); color: inherit; font-weight: inherit;
      transition: transform 0.15s, box-shadow 0.15s, border-color 0.15s;
    }
    .service:hover { transform: translateY(-3px); box-shadow: var(--shadow); border-color: #bcd9d0; text-decoration: none; color: inherit; }
    .top { display: flex; align-items: center; gap: 0.55rem; }
    .top .icon-tile { width: 40px; height: 40px; margin-inline-end: auto; }
    h3 { margin: 0.35rem 0 0; font-size: 1.06rem; }
    .desc { margin: 0; flex: 1; color: var(--c-muted); font-size: 0.9rem;
      display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
    .meta { display: flex; flex-wrap: wrap; gap: 0.35rem 0.9rem; font-size: 0.84rem; color: var(--c-muted); font-weight: 600; }
    .meta span { display: inline-flex; align-items: center; gap: 0.3rem; }
    .pro { max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .foot { display: flex; align-items: center; justify-content: space-between;
      padding-top: 0.8rem; margin-top: 0.2rem; border-top: 1px solid var(--c-border); }
    .price { font-size: 1.05rem; }
    .lock { display: inline-flex; align-items: center; gap: 0.35rem; font-size: 0.82rem; font-weight: 700;
      color: var(--c-accent-dark); background: var(--c-accent-soft); padding: 0.25rem 0.6rem; border-radius: 999px; }
    .locked-text { font-style: italic; }
    .go { display: inline-flex; align-items: center; gap: 0.25rem; color: var(--c-primary); font-weight: 700; font-size: 0.88rem; }
  `,
})
export class ServiceCard {
  readonly service = input.required<Service>();
  protected readonly icon = computed(() => CATEGORY_ICONS[this.service().category] ?? 'grid');
}
