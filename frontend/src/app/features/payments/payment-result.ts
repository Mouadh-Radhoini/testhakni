import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { errorMessage } from '../../core/api-error';
import { PaymentService, PaymentStatusResponse } from '../../core/services/payment.service';

@Component({
  imports: [RouterLink],
  template: `
    <section class="container">
      <article class="card">
        <h1>Paiement réussi</h1>
        @if (loading()) {
          <p>Vérification du paiement...</p>
        } @else if (error()) {
          <p class="alert error" role="alert">{{ error() }}</p>
        } @else if (payment()?.status === 'PAID') {
          <p class="alert success" role="status">Votre paiement a été confirmé.</p>
        } @else {
          <p>Le paiement est en cours de confirmation par Stripe. Cette page ne confirme pas le paiement.</p>
        }
        <a class="btn secondary" [routerLink]="['/bookings', bookingId()]">Voir la réservation</a>
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
      this.error.set('Réservation introuvable.');
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
  imports: [RouterLink],
  template: `
    <section class="container">
      <article class="card">
        <h1>Paiement annulé</h1>
        <p>Vous avez annulé le paiement. Aucun paiement n’est confirmé par cette page.</p>
        <a class="btn" [routerLink]="['/bookings', bookingId()]">Retourner à la réservation</a>
      </article>
    </section>
  `,
})
export class PaymentCancelPage {
  private readonly route = inject(ActivatedRoute);
  protected readonly bookingId = signal(this.route.snapshot.queryParamMap.get('bookingId') ?? '');
}
