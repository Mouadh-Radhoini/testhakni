import { Component, inject, input, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Booking, bookingLabels } from '../../core/marketplace.models';
import { AuthService } from '../../core/services/auth.service';
import { MarketplaceService } from '../../core/services/marketplace.service';
import { Feedback, PageState } from './page-state';

@Component({
  imports: [Feedback, RouterLink, DatePipe, ReactiveFormsModule],
  template: `<section class="container">
    <h1>{{ id() ? 'Détail de la réservation' : 'Mes réservations' }}</h1>
    <app-feedback [state]="state" />
    @for (b of bookings(); track b.id) {
      <article class="card mb">
        <h2>{{ b.title }}</h2>
        <p>{{ b.price }} DT · {{ b.proposedDate }} · {{ labels[b.status] }}</p>
        @if (!id()) {
          <a [routerLink]="['/bookings', b.id]">Voir la réservation</a>
        } @else {
          <p>{{ b.address }}</p>
          <ol>
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
          @if (auth.role() === 'CUSTOMER' && b.status === 'AWAITING_CONFIRMATION') {
            <button class="btn" [disabled]="state.loading()" (click)="confirm(b)">
              Confirmer la fin des travaux
            </button>
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
          <a routerLink="/bookings">Toutes mes réservations</a>
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
  confirm(b: Booking) {
    this.state.run(
      this.api.confirm(b.id),
      (updated) => this.bookings.set([updated]),
      'Travaux confirmés.',
    );
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
