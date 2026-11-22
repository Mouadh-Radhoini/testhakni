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
import { TranslatePipe, tr } from '../../core/i18n/i18n';

@Component({
  imports: [Feedback, RouterLink, DatePipe, ReactiveFormsModule, TranslatePipe],
  styles: `.grow-title { flex: 1; margin: 0; } .badge { margin-inline-start: auto; } .client-actions { margin: 1rem 0; } .cancel-box { margin-top: 0.75rem; }`,
  template: `<section class="container">
    <h1>{{ (id() ? 'bk.detail' : 'bk.mine') | t }}</h1>
    <app-feedback [state]="state" />
    @for (b of bookings(); track b.id) {
      <article class="card mb">
        <div class="row">
          <h2 class="grow-title">{{ b.title }}</h2>
          <span class="badge" [class]="b.status">{{ ('status.booking.' + b.status) | t }}</span>
        </div>
        <p class="muted"><strong class="price">{{ b.price }} {{ 'price.currency' | t }}</strong> · {{ 'bk.on' | t: { date: (b.proposedDate | date: 'dd/MM/yyyy') } }}</p>
        @if (!id()) {
          <a [routerLink]="['/bookings', b.id]">{{ 'bk.view' | t }}</a>
        } @else {
          <p>{{ b.address }}</p>
          <ol class="timeline">
            @for (h of b.history; track h.status) {
              <li>{{ ('status.booking.' + h.status) | t }} · {{ h.at | date: 'dd/MM/yyyy HH:mm' }}</li>
            }
          </ol>
          @if (
            auth.role() === 'PROVIDER' && (b.status === 'BOOKED' || b.status === 'IN_PROGRESS')
          ) {
            <button class="btn" [disabled]="state.loading()" (click)="advance(b)">
              {{ (b.status === 'BOOKED' ? 'bk.start' : 'bk.declareDone') | t }}
            </button>
          }
          @if (auth.role() === 'CUSTOMER' && isOpen(b)) {
            <div class="client-actions">
              <p class="muted small">
                @if (b.status === 'AWAITING_CONFIRMATION') {
                  {{ 'bk.awaitingNote' | t }}
                } @else {
                  {{ 'bk.followNote' | t }}
                }
              </p>
              <div class="row">
                @if (b.status === 'BOOKED') {
                  <button class="btn" type="button" [disabled]="state.loading()" (click)="customerSet(b, 'IN_PROGRESS')">
                    {{ 'bk.started' | t }}
                  </button>
                }
                <button class="btn" [class.secondary]="b.status === 'BOOKED'" type="button" [disabled]="state.loading()"
                        (click)="customerSet(b, 'COMPLETED')">
                  {{ (b.status === 'AWAITING_CONFIRMATION' ? 'bk.confirmEnd' : 'bk.markDone') | t }}
                </button>
                @if (!confirmingCancel()) {
                  <button class="btn danger" type="button" [disabled]="state.loading()" (click)="confirmingCancel.set(true)">
                    {{ 'bk.cancel' | t }}
                  </button>
                }
              </div>
              @if (confirmingCancel()) {
                <div class="alert info cancel-box" role="alertdialog">
                  <strong>{{ 'bk.cancelAsk' | t }}</strong>
                  {{ 'bk.cancelInfo' | t }}
                  <div class="row mt">
                    <button class="btn danger small" type="button" [disabled]="state.loading()" (click)="customerSet(b, 'CANCELLED')">
                      {{ 'bk.yesCancel' | t }}
                    </button>
                    <button class="btn secondary small" type="button" (click)="confirmingCancel.set(false)">{{ 'bk.keep' | t }}</button>
                  </div>
                </div>
              }
            </div>
          }
          @if (b.status === 'CANCELLED') {
            <p class="alert info">{{ 'bk.cancelled' | t }}</p>
          }
          @if (auth.role() === 'CUSTOMER' && b.status === 'COMPLETED' && !b.reviewed) {
            <form class="form" [formGroup]="form" (ngSubmit)="review(b)">
              <h2>{{ 'bk.yourReview' | t }}</h2>
              <label
                >{{ 'bk.rating' | t }}<select formControlName="rating">
                  @for (n of [1, 2, 3, 4, 5]; track n) {
                    <option [ngValue]="n">{{ n }}</option>
                  }
                </select></label
              >
              <label
                >{{ 'bk.comment' | t }}<textarea maxlength="2000" formControlName="comment"></textarea>
              </label>
              <button class="btn" [disabled]="form.invalid || state.loading()">
                {{ 'bk.publish' | t }}
              </button>
            </form>
          }
          <div class="row mt">
            <button class="btn secondary" type="button" (click)="chat(b)">
              {{ (auth.role() === 'PROVIDER' ? 'chat.withCustomer' : 'chat.toProvider') | t }}
            </button>
            <a routerLink="/bookings"><span class="flip">←</span> {{ 'bk.all' | t }}</a>
          </div>
        }
      </article>
    } @empty {
      @if (!state.loading() && !state.error()) {
        <p>{{ 'bk.none' | t }}</p>
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
      tr('bk.msgProgress'),
    );
  }
  readonly confirmingCancel = signal(false);
  /** Reservation encore en cours de route : le client peut la faire avancer ou l'annuler. */
  isOpen(b: Booking): boolean {
    return b.status === 'BOOKED' || b.status === 'IN_PROGRESS' || b.status === 'AWAITING_CONFIRMATION';
  }
  customerSet(b: Booking, status: 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED') {
    const messages = {
      IN_PROGRESS: tr('bk.msgInProgress'),
      COMPLETED: tr('bk.msgCompleted'),
      CANCELLED: tr('bk.msgCancelled'),
    };
    this.confirmingCancel.set(false);
    this.state.run(this.api.customerStatus(b.id, status), (updated) => this.bookings.set([updated]), messages[status]);
  }
  review(b: Booking) {
    const value = this.form.getRawValue();
    this.state.run(
      this.api.review(b.id, value.rating, value.comment),
      () => this.bookings.set([{ ...b, reviewed: true }]),
      tr('bk.msgReviewed'),
    );
  }
}
