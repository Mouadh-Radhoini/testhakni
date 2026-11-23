import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { errorMessage } from '../../core/api-error';
import { PaymentService, PaymentStatusResponse } from '../../core/services/payment.service';
import { TranslatePipe, tr } from '../../core/i18n/i18n';

@Component({
  imports: [RouterLink, TranslatePipe],
  template: `
    <section class="container">
      <article class="card">
        <h1>{{ 'pay.successTitle' | t }}</h1>
        @if (loading()) {
          <p>{{ 'pay.checking' | t }}</p>
        } @else if (error()) {
          <p class="alert error" role="alert">{{ error() }}</p>
        } @else if (payment()?.status === 'PAID') {
          <p class="alert success" role="status">{{ 'pay.confirmed' | t }}</p>
        } @else {
          <p>{{ 'pay.processing' | t }}</p>
        }
        <a class="btn secondary" [routerLink]="['/bookings', bookingId()]">{{ 'pay.viewBooking' | t }}</a>
      </article>
    </section>
  `,
})
export class PaymentSuccessPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly payments = inject(PaymentService);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly payment = signal<PaymentStatusResponse | null>(null);
  protected readonly bookingId = signal('');

  ngOnInit(): void {
    const bookingId = this.route.snapshot.queryParamMap.get('bookingId') ?? '';
    this.bookingId.set(bookingId);
    if (!bookingId) {
      this.loading.set(false);
      this.error.set(tr('pay.notFound'));
      return;
    }
    this.payments.status(bookingId).subscribe({
      next: (payment) => {
        this.payment.set(payment);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }
}

@Component({
  imports: [RouterLink, TranslatePipe],
  template: `
    <section class="container">
      <article class="card">
        <h1>{{ 'pay.cancelTitle' | t }}</h1>
        <p>{{ 'pay.cancelText' | t }}</p>
        <a class="btn" [routerLink]="['/bookings', bookingId()]">{{ 'pay.back' | t }}</a>
      </article>
    </section>
  `,
})
export class PaymentCancelPage {
  private readonly route = inject(ActivatedRoute);
  protected readonly bookingId = signal(this.route.snapshot.queryParamMap.get('bookingId') ?? '');
}
