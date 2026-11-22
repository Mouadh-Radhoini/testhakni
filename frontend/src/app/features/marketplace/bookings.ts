import { Component, inject, input, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Booking, bookingLabels } from '../../core/marketplace.models';
import { AuthService } from '../../core/services/auth.service';
import { MarketplaceService } from '../../core/services/marketplace.service';
import { MessagingService } from '../../core/services/messaging.service';
import { Router } from '@angular/router';
import { Feedback, PageState } from './page-state';

@Component({
  imports: [Feedback, RouterLink, DatePipe, ReactiveFormsModule],
  styles: `.grow-title { flex: 1; margin: 0; } .badge { margin-inline-start: auto; } .client-actions { margin: 1rem 0; } .cancel-box { margin-top: 0.75rem; }`,
  template: `<section class="container">
    <h1>{{ id() ? 'Détail de la réservation' : 'Mes réservations' }}</h1>
    <app-feedback [state]="state" />
    @for (b of bookings(); track b.id) {
      <article class="card mb">
        <div class="row">
          <h2 class="grow-title">{{ b.title }}</h2>
          <span class="badge" [class]="b.status">{{ labels[b.status] }}</span>
        </div>
        <p class="muted"><strong class="price">{{ b.price }} DT</strong> · Intervention le {{ b.proposedDate | date: 'dd/MM/yyyy' }}</p>
        @if (!id()) {
          <a [routerLink]="['/bookings', b.id]">Voir la réservation</a>
        } @else {
          <p>{{ b.address }}</p>
          <ol class="timeline">
            @for (h of b.history; track h.status) {
              <li>{{ labels[h.status] }} · {{ h.at | date: 'dd/MM/yyyy HH:mm' }}</li>
            }
          </ol>
          @if (
            auth.role() === 'PROVIDER' && (b.status === 'BOOKED' || b.status === 'IN_PROGRESS')
          ) {
            <button class="btn" [disabled]="state.loading()" (click)="advance(b)">
              {{ b.status === 'BOOKED' ? 'Démarrer le travail' : 'Déclarer le travail terminé' }}
            </button>
          }
          @if (auth.role() === 'CUSTOMER' && isOpen(b)) {
            <div class="client-actions">
              <p class="muted small">
                @if (b.status === 'AWAITING_CONFIRMATION') {
                  Le prestataire indique avoir terminé. Vérifiez le travail avant de confirmer.
                } @else {
                  Vous suivez l’avancement : mettez à jour l’état de la prestation.
                }
              </p>
              <div class="row">
                @if (b.status === 'BOOKED') {
                  <button class="btn" type="button" [disabled]="state.loading()" (click)="customerSet(b, 'IN_PROGRESS')">
                    Le prestataire a commencé
                  </button>
                }
                <button class="btn" [class.secondary]="b.status === 'BOOKED'" type="button" [disabled]="state.loading()"
                        (click)="customerSet(b, 'COMPLETED')">
                  {{ b.status === 'AWAITING_CONFIRMATION' ? 'Confirmer la fin des travaux' : 'Marquer comme terminée' }}
                </button>
                @if (!confirmingCancel()) {
                  <button class="btn danger" type="button" [disabled]="state.loading()" (click)="confirmingCancel.set(true)">
                    Annuler la réservation
                  </button>
                }
              </div>
              @if (confirmingCancel()) {
                <div class="alert info cancel-box" role="alertdialog">
                  <strong>Annuler cette réservation ?</strong>
                  Le prestataire sera prévenu et la demande sera annulée. Vous ne pourrez plus revenir en arrière.
                  <div class="row mt">
                    <button class="btn danger small" type="button" [disabled]="state.loading()" (click)="customerSet(b, 'CANCELLED')">
                      Oui, annuler
                    </button>
                    <button class="btn secondary small" type="button" (click)="confirmingCancel.set(false)">Non, garder</button>
                  </div>
                </div>
              }
            </div>
          }
          @if (b.status === 'CANCELLED') {
            <p class="alert info">Cette réservation a été annulée.</p>
          }
          @if (auth.role() === 'CUSTOMER' && b.status === 'COMPLETED' && !b.reviewed) {
            <form class="form" [formGroup]="form" (ngSubmit)="review(b)">
              <h2>Votre avis</h2>
              <label
                >Note sur cinq<select formControlName="rating">
                  @for (n of [1, 2, 3, 4, 5]; track n) {
                    <option [ngValue]="n">{{ n }}</option>
                  }
                </select></label
              >
              <label
                >Commentaire<textarea maxlength="2000" formControlName="comment"></textarea>
              </label>
              <button class="btn" [disabled]="form.invalid || state.loading()">
                Publier l’avis
              </button>
            </form>
          }
          <div class="row mt">
            <button class="btn secondary" type="button" (click)="chat(b)">
              {{ auth.role() === 'PROVIDER' ? 'Écrire au client' : 'Écrire au prestataire' }}
            </button>
            <a routerLink="/bookings">← Toutes mes réservations</a>
          </div>
        }
      </article>
    } @empty {
      @if (!state.loading() && !state.error()) {
        <p>Aucune réservation pour le moment.</p>
      }
    }
  </section>`,
})
export class BookingsPage implements OnInit {
  readonly id = input<string>();
  readonly state = new PageState();
  readonly auth = inject(AuthService);
  private readonly api = inject(MarketplaceService);
  readonly labels = bookingLabels;
  private readonly messaging = inject(MessagingService);
  private readonly router = inject(Router);
  chat(b: Booking) {
    this.state.run(this.messaging.openForBooking(b.id), (c) => this.router.navigate(['/messages', c.id]));
  }
  readonly bookings = signal<Booking[]>([]);
  readonly form = inject(FormBuilder).nonNullable.group({
    rating: [5, Validators.required],
    comment: ['', Validators.required],
  });
  ngOnInit() {
    const id = this.id();
    if (id) this.state.run(this.api.booking(id), (b) => this.bookings.set([b]));
    else this.state.run(this.api.bookings(), (list) => this.bookings.set(list));
  }
  advance(b: Booking) {
    this.state.run(
      this.api.progress(b.id, b.status === 'BOOKED' ? 'IN_PROGRESS' : 'AWAITING_CONFIRMATION'),
      (updated) => this.bookings.set([updated]),
      'Progression enregistrée.',
    );
  }
  readonly confirmingCancel = signal(false);
  /** Reservation encore en cours de route : le client peut la faire avancer ou l'annuler. */
  isOpen(b: Booking): boolean {
    return b.status === 'BOOKED' || b.status === 'IN_PROGRESS' || b.status === 'AWAITING_CONFIRMATION';
  }
  customerSet(b: Booking, status: 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED') {
    const messages = {
      IN_PROGRESS: 'La prestation est marquée en cours.',
      COMPLETED: 'Prestation terminée : vous pouvez laisser un avis.',
      CANCELLED: 'Réservation annulée.',
    };
    this.confirmingCancel.set(false);
    this.state.run(this.api.customerStatus(b.id, status), (updated) => this.bookings.set([updated]), messages[status]);
  }
  review(b: Booking) {
    const value = this.form.getRawValue();
    this.state.run(
      this.api.review(b.id, value.rating, value.comment),
      () => this.bookings.set([{ ...b, reviewed: true }]),
      'Avis publié.',
    );
  }
}
