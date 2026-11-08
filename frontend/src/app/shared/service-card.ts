import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Service } from '../core/models';
import { PricePipe } from './price.pipe';

/** Carte d'un service dans les listes (recherche, profil prestataire). */
@Component({
  selector: 'app-service-card',
  imports: [RouterLink, PricePipe],
  template: `
    <a class="card service" [routerLink]="['/services', service().id]">
      <div class="row">
        <span class="badge">{{ service().categoryLabel }}</span>
        <span class="spacer"></span>
        <span class="price">{{ service().price | price: service().priceType }}</span>
      </div>
      <h3>{{ service().title }}</h3>
      <p class="muted small desc">{{ service().description }}</p>
      <p class="small">
        📍 {{ service().city }}
        @if (service().provider; as p) { · {{ p.businessName }} }
      </p>
    </a>
  `,
  styles: `
    .service { display: flex; flex-direction: column; gap: 0.4rem; color: inherit; height: 100%; }
    .service:hover { border-color: var(--c-primary); text-decoration: none; }
    h3 { margin: 0.2rem 0 0; font-size: 1.05rem; }
    .desc { margin: 0; flex: 1; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
    p { margin: 0; }
  `,
})
export class ServiceCard {
  readonly service = input.required<Service>();
}
