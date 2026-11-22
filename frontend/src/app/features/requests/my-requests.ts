import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, input, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { errorMessage } from '../../core/api-error';
import { REQUEST_STATUS_LABELS, RequestStatus, ServiceRequest } from '../../core/models';
import { RequestService } from '../../core/services/request.service';
import { TranslatePipe, tr } from '../../core/i18n/i18n';

/** Liste des demandes du client, filtrable par statut, avec annulation (REQ-02). FE-06 */
@Component({
  selector: 'app-my-requests',
  imports: [RouterLink, DatePipe, DecimalPipe, TranslatePipe],
  template: `
    <section class="container">
      <div class="row mb">
        <h1>{{ 'nav.myRequests' | t }}</h1>
        <span class="spacer"></span>
        <a class="btn" routerLink="/requests/new">{{ 'rq.new' | t }}</a>
      </div>

      @if (created()) { <div class="alert success mb">{{ 'rq.sent' | t }}</div> }
      @if (error()) { <div class="alert error mb">{{ error() }}</div> }

      <div class="row mb" role="group">
        @for (f of filters; track f.value) {
          <button type="button" class="btn small" [class.secondary]="status() !== f.value" (click)="load(f.value)">
            {{ f.label | t }}
          </button>
        }
      </div>

      @if (loading()) {
        <p class="muted">{{ 'common.loading' | t }}</p>
      } @else if (requests().length === 0) {
        <div class="card empty">
          <p>{{ 'rq.empty' | t }}</p>
          <a class="btn" routerLink="/services">{{ 'rq.browse' | t }}</a>
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
                  <button class="btn danger small" (click)="cancel(r)">{{ 'common.cancel' | t }}</button>
                }
              </div>
              <p class="muted small">
                {{ ('cat.' + r.category) | t }} · {{ r.city }}
                @if (r.preferredDate) { · {{ 'rq.wished' | t: { date: (r.preferredDate | date: 'dd/MM/yyyy') } }} }
                @if (r.budget !== null) { · {{ 'rq.budget' | t: { n: (r.budget | number: '1.0-2') } }} }
                · {{ 'rq.created' | t: { date: (r.createdAt | date: 'dd/MM/yyyy') } }}
              </p>
              <p class="desc">{{ r.description }}</p>
              <a [routerLink]="['/requests', r.id, 'offers']">{{ 'rq.compare' | t }}</a>
              @if (r.serviceId) {
                <a class="small" [routerLink]="['/services', r.serviceId]">{{ 'rq.seeService' | t }}</a>
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
    { value: '', label: 'search.all' },
    { value: 'OPEN', label: 'rq.f.OPEN' },
    { value: 'IN_PROGRESS', label: 'rq.f.IN_PROGRESS' },
    { value: 'COMPLETED', label: 'rq.f.COMPLETED' },
    { value: 'CANCELLED', label: 'rq.f.CANCELLED' },
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
    if (!confirm(tr('rq.confirmCancel', { title: r.title }))) return;
    this.requestService.cancel(r.id).subscribe({
      next: () => this.load(this.status()),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
