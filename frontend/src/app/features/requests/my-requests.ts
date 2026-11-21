import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, input, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { errorMessage } from '../../core/api-error';
import { REQUEST_STATUS_LABELS, RequestStatus, ServiceRequest } from '../../core/models';
import { RequestService } from '../../core/services/request.service';

/** Liste des demandes du client, filtrable par statut, avec annulation (REQ-02). FE-06 */
@Component({
  selector: 'app-my-requests',
  imports: [RouterLink, DatePipe, DecimalPipe],
  template: `
    <section class="container">
      <div class="row mb">
        <h1>Mes demandes</h1>
        <span class="spacer"></span>
        <a class="btn" routerLink="/requests/new">+ Nouvelle demande</a>
      </div>

      @if (created()) { <div class="alert success mb">Votre demande a bien été envoyée.</div> }
      @if (error()) { <div class="alert error mb">{{ error() }}</div> }

      <div class="row mb tabs" role="tablist">
        @for (f of filters; track f.value) {
          <button type="button" class="btn small" [class.secondary]="status() !== f.value" (click)="load(f.value)">
            {{ f.label }}
          </button>
        }
      </div>

      @if (loading()) {
        <p class="muted">Chargement…</p>
      } @else if (requests().length === 0) {
        <div class="card empty">
          <p>Aucune demande pour le moment.</p>
          <a class="btn" routerLink="/services">Parcourir les services</a>
        </div>
      } @else {
        <div class="list">
          @for (r of requests(); track r.id) {
            <article class="card">
              <div class="row">
                <strong>{{ r.title }}</strong>
                <span class="badge" [class]="r.status">{{ statusLabel[r.status] }}</span>
                <span class="spacer"></span>
                @if (r.status === 'OPEN') {
                  <button class="btn danger small" (click)="cancel(r)">Annuler</button>
                }
              </div>
              <p class="muted small">
                {{ r.categoryLabel }} · 📍 {{ r.city }}
                @if (r.preferredDate) { · souhaitée le {{ r.preferredDate | date: 'dd/MM/yyyy' }} }
                @if (r.budget !== null) { · budget {{ r.budget | number: '1.0-2' }} DT }
                · créée le {{ r.createdAt | date: 'dd/MM/yyyy' }}
              </p>
              <p class="desc">{{ r.description }}</p>
              <a [routerLink]="['/requests', r.id, 'offers']">Comparer les offres</a>
              @if (r.serviceId) {
                <a class="small" [routerLink]="['/services', r.serviceId]">Voir le service concerné</a>
              }
            </article>
          }
        </div>
      }
    </section>
  `,
  styles: `
    .list { display: flex; flex-direction: column; gap: 0.75rem; }
    .desc { margin: 0.4rem 0; white-space: pre-line; }
    p { margin: 0.3rem 0 0; }
  `,
})
export class MyRequests implements OnInit {
  private readonly requestService = inject(RequestService);

  /** ?created=1 apres creation. */
  readonly created = input<string>();

  protected readonly statusLabel = REQUEST_STATUS_LABELS;
  protected readonly filters: { value: RequestStatus | ''; label: string }[] = [
    { value: '', label: 'Toutes' },
    { value: 'OPEN', label: 'Ouvertes' },
    { value: 'IN_PROGRESS', label: 'En cours' },
    { value: 'COMPLETED', label: 'Terminées' },
    { value: 'CANCELLED', label: 'Annulées' },
  ];
  protected readonly status = signal<RequestStatus | ''>('');
  protected readonly requests = signal<ServiceRequest[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.load('');
  }

  load(status: RequestStatus | ''): void {
    this.status.set(status);
    this.loading.set(true);
    this.requestService.mine(status).subscribe({
      next: (list) => {
        this.requests.set(list);
        this.loading.set(false);
      },
      error: (e) => {
        this.error.set(errorMessage(e));
        this.loading.set(false);
      },
    });
  }

  cancel(r: ServiceRequest): void {
    if (!confirm(`Annuler la demande « ${r.title} » ?`)) return;
    this.requestService.cancel(r.id).subscribe({
      next: () => this.load(this.status()),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
