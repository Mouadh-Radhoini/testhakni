import { Component, inject, input, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Offer, offerLabels } from '../../core/marketplace.models';
import { MarketplaceService } from '../../core/services/marketplace.service';
import { Feedback, PageState } from './page-state';

@Component({
  imports: [Feedback],
  template: `<section class="container">
    <h1>{{ id() ? 'Comparer les offres' : 'Mes offres' }}</h1>
    <app-feedback [state]="state" />
    @for (o of offers(); track o.id) {
      <article class="card mb">
        <h2>{{ id() ? o.businessName : o.requestTitle }} · {{ o.price }} DT</h2>
        <p>Intervention proposée le {{ o.proposedDate }} · {{ labels[o.status] }}</p>
        <p>{{ o.description }}</p>
        @if (o.status === 'SUBMITTED') {
          <button class="btn" [disabled]="state.loading()" (click)="decide(o)">
            {{ id() ? 'Accepter cette offre' : 'Retirer cette offre' }}
          </button>
        }
      </article>
    } @empty {
      @if (!state.loading()) {
        <p>Aucune offre pour le moment.</p>
      }
    }
  </section>`,
})
export class OffersPage implements OnInit {
  readonly id = input<string>();
  private readonly api = inject(MarketplaceService);
  private readonly router = inject(Router);
  readonly state = new PageState();
  readonly offers = signal<Offer[]>([]);
  readonly labels = offerLabels;
  ngOnInit() {
    this.load();
  }
  load() {
    this.state.run(this.api.offers(this.id()), (list) => this.offers.set(list));
  }
  decide(o: Offer) {
    if (this.id())
      this.state.run(
        this.api.accept(o),
        (b) => this.router.navigate(['/bookings', b.id]),
        '',
        () =>
          this.api
            .offers(this.id())
            .subscribe({ next: (list) => this.offers.set(list), error: () => {} }),
      );
    else this.state.run(this.api.withdraw(o.id), () => this.load(), 'Offre retirée.');
  }
}
