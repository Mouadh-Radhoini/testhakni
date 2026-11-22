import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { OpenRequest } from '../../core/marketplace.models';
import { MarketplaceService } from '../../core/services/marketplace.service';
import { Feedback, PageState } from './page-state';
import { TranslatePipe, tr } from '../../core/i18n/i18n';

@Component({
  imports: [Feedback, ReactiveFormsModule, TranslatePipe],
  template: `<section class="container">
    <h1>{{ 'nav.openRequests' | t }}</h1>
    <app-feedback [state]="state" />
    @for (r of requests(); track r.id) {
      <article class="card mb">
        <h2>{{ r.title }}</h2>
        <p>{{ ('cat.' + r.category) | t }} · {{ r.city }}</p>
        <p>{{ r.description }}</p>
        @if (r.budget != null) {
          <p>{{ 'or.budget' | t: { n: r.budget } }}</p>
        }
        @if (r.preferredDate) {
          <p>{{ 'or.date' | t: { d: r.preferredDate } }}</p>
        }
        @if (selected() === r.id) {
          <form class="form" [formGroup]="form" (ngSubmit)="submit(r.id)">
            <label
              >{{ 'sf.price' | t }}<input type="number" min="0.01" step="0.01" formControlName="price"
            /></label>
            <label
              >{{ 'or.proposedDate' | t }}<input type="date" [min]="today" formControlName="proposedDate"
            /></label>
            <label
              >{{ 'sf.description' | t }}<textarea formControlName="description" maxlength="2000"></textarea>
            </label>
            <button class="btn" [disabled]="form.invalid || state.loading()">
              {{ 'or.send' | t }}
            </button>
          </form>
        } @else {
          <button class="btn" (click)="select(r.id)">{{ 'or.propose' | t }}</button>
        }
      </article>
    } @empty {
      @if (!state.loading()) {
        <p>{{ 'or.empty' | t }}</p>
      }
    }
  </section>`,
})
export class OpenRequests implements OnInit {
  private readonly api = inject(MarketplaceService);
  readonly state = new PageState();
  readonly today = new Date().toLocaleDateString('en-CA');
  readonly selected = signal('');
  readonly requests = signal<OpenRequest[]>([]);
  readonly form = inject(FormBuilder).nonNullable.group({
    price: [0, [Validators.required, Validators.min(0.01)]],
    proposedDate: ['', Validators.required],
    description: ['', [Validators.required, Validators.maxLength(2000)]],
  });
  ngOnInit() {
    this.state.run(this.api.open(), (list) => this.requests.set(list));
  }
  select(id: string) {
    this.form.reset();
    this.selected.set(id);
  }
  submit(id: string) {
    if (this.form.invalid) return;
    this.state.run(
      this.api.submit(id, this.form.getRawValue()),
      () => this.selected.set(''),
      tr('or.sent'),
    );
  }
}
