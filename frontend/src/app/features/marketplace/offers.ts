import { Component, inject, input, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Offer, offerLabels } from '../../core/marketplace.models';
import { MarketplaceService } from '../../core/services/marketplace.service';
import { MessagingService } from '../../core/services/messaging.service';
import { Icon } from '../../shared/icon';
import { Feedback, PageState } from './page-state';
import { TranslatePipe, tr } from '../../core/i18n/i18n';

@Component({
  imports: [Feedback, Icon, TranslatePipe],
  styles: `.title { flex: 1; margin: 0; }`,
  template: `<section class="container">
    <h1>{{ (id() ? 'of.compareTitle' : 'nav.myOffers') | t }}</h1>
    <app-feedback [state]="state" />
    @for (o of offers(); track o.id) {
      <article class="card mb">
        <div class="row">
          <h2 class="title">{{ id() ? o.businessName : o.requestTitle }}</h2>
          <span class="badge" [class]="o.status">{{ ('status.offer.' + o.status) | t }}</span>
        </div>
        <p class="muted"><strong class="price">{{ o.price }} DT</strong> · {{ 'of.proposedOn' | t: { date: o.proposedDate } }}</p>
        <p>{{ o.description }}</p>
        <div class="row">
          @if (o.status === 'SUBMITTED') {
            <button class="btn" [disabled]="state.loading()" (click)="decide(o)">
              {{ (id() ? 'of.accept' : 'of.withdraw') | t }}
            </button>
          }
          @if (o.status !== 'CANCELLED') {
            <button class="btn secondary" type="button" (click)="chat(o)">
              <app-icon name="chat" [size]="16" /> {{ (id() ? 'chat.withProvider' : 'chat.withCustomer') | t }}
            </button>
          }
        </div>
      </article>
    } @empty {
      @if (!state.loading()) {
        <p>{{ 'of.none' | t }}</p>
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
  private readonly messaging = inject(MessagingService);
  chat(o: Offer) {
    this.state.run(this.messaging.openForOffer(o.id), (c) => this.router.navigate(['/messages', c.id]));
  }
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
    else this.state.run(this.api.withdraw(o.id), () => this.load(), tr('of.withdrawn'));
  }
}
