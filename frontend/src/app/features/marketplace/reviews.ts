import { DecimalPipe } from '@angular/common';
import { Component, inject, input, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Reviews } from '../../core/marketplace.models';
import { AuthService } from '../../core/services/auth.service';
import { MarketplaceService } from '../../core/services/marketplace.service';
import { Feedback, PageState } from './page-state';
import { TranslatePipe, tr } from '../../core/i18n/i18n';

@Component({
  selector: 'app-reviews',
  imports: [Feedback, DecimalPipe, ReactiveFormsModule, TranslatePipe],
  template: `<section class="card mt">
    <h2>{{ 'rv.title' | t }}</h2>
    <app-feedback [state]="state" />
    @if (result(); as r) {
      @if (r.count) {
        <p>{{ r.average | number: '1.0-1' }}/5 · {{ 'provider.reviewsCount' | t: { n: r.count } }}</p>
      }
      @for (entry of r.reviews; track entry.id) {
        <article>
          <strong>{{ entry.rating }}/5</strong>
          <p>{{ entry.comment }}</p>
          @if (auth.user()) {
            <button class="btn secondary small" (click)="select('REVIEW', entry.id)">
              {{ 'rv.reportReview' | t }}
            </button>
          }
        </article>
      } @empty {
        <p>{{ 'rv.none' | t }}</p>
      }
    }
    @if (group() === 'services' && auth.user()) {
      <button class="btn secondary small" (click)="select('SERVICE', id())">
        {{ 'rv.reportService' | t }}
      </button>
    }
    @if (target()) {
      <form class="form mt" [formGroup]="reportForm" (ngSubmit)="report()">
        <label
          >{{ 'rv.reason' | t }}<textarea formControlName="reason" maxlength="1000"></textarea>
        </label>
        <button class="btn" [disabled]="reason.invalid || state.loading()">
          {{ 'rv.send' | t }}
        </button>
      </form>
    }
  </section>`,
})
export class ReviewsPanel implements OnInit {
  readonly group = input.required<'services' | 'providers'>();
  readonly id = input.required<string>();
  readonly auth = inject(AuthService);
  private readonly api = inject(MarketplaceService);
  readonly state = new PageState();
  readonly result = signal<Reviews | null>(null);
  readonly target = signal<{ type: 'SERVICE' | 'REVIEW'; id: string } | null>(null);
  readonly reason = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(1000)],
  });
  readonly reportForm = new FormGroup({ reason: this.reason });
  ngOnInit() {
    this.state.run(this.api.reviews(this.group(), this.id()), (data) => this.result.set(data));
  }
  select(type: 'SERVICE' | 'REVIEW', id: string) {
    this.reason.reset();
    this.target.set({ type, id });
  }
  report() {
    const target = this.target();
    if (!target || this.reason.invalid) return;
    this.state.run(
      this.api.report(target.type, target.id, this.reason.value),
      () => this.target.set(null),
      tr('rv.msgSent'),
    );
  }
}
