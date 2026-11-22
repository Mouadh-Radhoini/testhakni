import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { errorMessage } from '../../core/api-error';
import { Service, SERVICE_STATUS_LABELS } from '../../core/models';
import { ProviderService } from '../../core/services/provider.service';
import { PricePipe } from '../../shared/price.pipe';
import { TranslatePipe, tr } from '../../core/i18n/i18n';

/** Liste des services du prestataire : activer/desactiver, modifier, supprimer. FE-05 */
@Component({
  selector: 'app-my-services',
  imports: [RouterLink, PricePipe, TranslatePipe],
  template: `
    <section class="container">
      <div class="row mb">
        <h1>{{ 'nav.myServices' | t }}</h1>
        <span class="spacer"></span>
        <a class="btn" routerLink="/provider/services/new">{{ 'ps.new' | t }}</a>
      </div>

      @if (error()) { <div class="alert error mb">{{ error() }}</div> }

      @if (loading()) {
        <p class="muted">{{ 'common.loading' | t }}</p>
      } @else if (services().length === 0) {
        <div class="card empty">
          <p>{{ 'ps.empty' | t }}</p>
          <a class="btn" routerLink="/provider/services/new">{{ 'ps.first' | t }}</a>
        </div>
      } @else {
        <div class="list">
          @for (s of services(); track s.id) {
            <article class="card item">
              <div class="info">
                <div class="row">
                  <strong>{{ s.title }}</strong>
                  <span class="badge" [class]="s.status">{{ statusLabel[s.status] }}</span>
                </div>
                <span class="muted small">{{ ('cat.' + s.category) | t }} · {{ s.city }} · {{ s.price | price: s.priceType! }}</span>
              </div>
              <div class="row">
                @if (s.status === 'ACTIVE') {
                  <button class="btn secondary small" (click)="setStatus(s, 'INACTIVE')">{{ 'ps.deactivate' | t }}</button>
                } @else if (s.status === 'INACTIVE') {
                  <button class="btn secondary small" (click)="setStatus(s, 'ACTIVE')">{{ 'ps.activate' | t }}</button>
                }
                <a class="btn secondary small" [routerLink]="['/provider/services', s.id, 'edit']">{{ 'ps.edit' | t }}</a>
                <button class="btn danger small" (click)="remove(s)">{{ 'ps.delete' | t }}</button>
              </div>
            </article>
          }
        </div>
      }
    </section>
  `,
  styles: `
    .list { display: flex; flex-direction: column; gap: 0.75rem; }
    .item { display: flex; flex-wrap: wrap; gap: 0.75rem; align-items: center; justify-content: space-between; }
    .info { display: flex; flex-direction: column; gap: 0.2rem; }
  `,
})
export class MyServices implements OnInit {
  private readonly providers = inject(ProviderService);

  protected readonly statusLabel = SERVICE_STATUS_LABELS;
  protected readonly services = signal<Service[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.providers.myServices().subscribe({
      next: (list) => {
        this.services.set(list);
        this.loading.set(false);
      },
      error: (e) => {
        this.error.set(errorMessage(e));
        this.loading.set(false);
      },
    });
  }

  setStatus(s: Service, status: 'ACTIVE' | 'INACTIVE'): void {
    this.providers.setStatus(s.id, status).subscribe({
      next: (updated) => this.services.update((list) => list.map((x) => (x.id === updated.id ? updated : x))),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  remove(s: Service): void {
    if (!confirm(tr('ps.confirmDelete', { title: s.title }))) return;
    this.providers.deleteService(s.id).subscribe({
      next: () => this.services.update((list) => list.filter((x) => x.id !== s.id)),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
