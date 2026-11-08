import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { errorMessage } from '../../core/api-error';
import { Service, SERVICE_STATUS_LABELS } from '../../core/models';
import { ProviderService } from '../../core/services/provider.service';
import { PricePipe } from '../../shared/price.pipe';

/** Liste des services du prestataire : activer/desactiver, modifier, supprimer. FE-05 */
@Component({
  selector: 'app-my-services',
  imports: [RouterLink, PricePipe],
  template: `
    <section class="container">
      <div class="row mb">
        <h1>Mes services</h1>
        <span class="spacer"></span>
        <a class="btn" routerLink="/provider/services/new">+ Nouveau service</a>
      </div>

      @if (error()) { <div class="alert error mb">{{ error() }}</div> }

      @if (loading()) {
        <p class="muted">Chargement…</p>
      } @else if (services().length === 0) {
        <div class="card empty">
          <p>Vous n'avez encore publié aucun service.</p>
          <a class="btn" routerLink="/provider/services/new">Publier mon premier service</a>
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
                <span class="muted small">{{ s.categoryLabel }} · {{ s.city }} · {{ s.price | price: s.priceType }}</span>
              </div>
              <div class="row">
                @if (s.status === 'ACTIVE') {
                  <button class="btn secondary small" (click)="setStatus(s, 'INACTIVE')">Désactiver</button>
                } @else if (s.status === 'INACTIVE') {
                  <button class="btn secondary small" (click)="setStatus(s, 'ACTIVE')">Activer</button>
                }
                <a class="btn secondary small" [routerLink]="['/provider/services', s.id, 'edit']">Modifier</a>
                <button class="btn danger small" (click)="remove(s)">Supprimer</button>
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
    if (!confirm(`Supprimer définitivement « ${s.title} » ?`)) return;
    this.providers.deleteService(s.id).subscribe({
      next: () => this.services.update((list) => list.filter((x) => x.id !== s.id)),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
