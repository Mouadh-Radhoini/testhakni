import { Component, inject, input, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { applyServerErrors, controlError, errorMessage } from '../../core/api-error';
import { ServiceCategory } from '../../core/models';
import { CatalogService } from '../../core/services/catalog.service';
import { RequestService } from '../../core/services/request.service';

/**
 * Creation d'une demande (REQ-02 cote UI). FE-06
 * Peut etre pre-remplie depuis la page d'un service (?serviceId=&category=&city=&title=).
 */
@Component({
  selector: 'app-request-form',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <section class="container narrow">
      <p><a routerLink="/requests">← Mes demandes</a></p>
      <div class="card">
        <h1>Nouvelle demande</h1>
        @if (title()) {
          <p class="alert info">Demande liée au service « {{ title() }} »</p>
        }
        <form class="form" [formGroup]="form" (ngSubmit)="save()" novalidate>
          @if (error()) { <div class="alert error">{{ error() }}</div> }
          <div class="field">
            <label for="title">Titre de la demande</label>
            <input id="title" formControlName="title" placeholder="Ex. Fuite sous l'évier" />
            @if (err('title'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <div class="field">
            <label for="description">Décrivez votre besoin</label>
            <textarea id="description" formControlName="description"></textarea>
            @if (err('description'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <div class="form-row">
            <div class="field">
              <label for="category">Catégorie</label>
              <select id="category" formControlName="category">
                <option value="" disabled>Choisir…</option>
                @for (c of categories(); track c.code) {
                  <option [value]="c.code">{{ c.label }}</option>
                }
              </select>
              @if (err('category'); as e) { <span class="error">{{ e }}</span> }
            </div>
            <div class="field">
              <label for="city">Ville</label>
              <input id="city" formControlName="city" />
              @if (err('city'); as e) { <span class="error">{{ e }}</span> }
            </div>
          </div>
          <div class="field">
            <label for="address">Adresse <span class="muted">(optionnel)</span></label>
            <input id="address" formControlName="address" />
          </div>
          <div class="form-row">
            <div class="field">
              <label for="preferredDate">Date souhaitée <span class="muted">(optionnel)</span></label>
              <input id="preferredDate" type="date" [min]="today" formControlName="preferredDate" />
              @if (err('preferredDate'); as e) { <span class="error">{{ e }}</span> }
            </div>
            <div class="field">
              <label for="budget">Budget en DT <span class="muted">(optionnel)</span></label>
              <input id="budget" type="number" min="0" formControlName="budget" />
              @if (err('budget'); as e) { <span class="error">{{ e }}</span> }
            </div>
          </div>
          <div class="row">
            <button class="btn" type="submit" [disabled]="saving()">Envoyer la demande</button>
            <a class="btn secondary" routerLink="/requests">Annuler</a>
          </div>
        </form>
      </div>
    </section>
  `,
})
export class RequestForm implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly requests = inject(RequestService);
  private readonly router = inject(Router);

  readonly serviceId = input<string>();
  readonly category = input<string>();
  readonly city = input<string>();
  readonly title = input<string>();

  protected readonly categories = toSignal(inject(CatalogService).categories(), { initialValue: [] });
  protected readonly today = new Date().toISOString().slice(0, 10);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.group({
    title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(2000)]],
    category: this.fb.control<ServiceCategory | ''>('', Validators.required),
    city: ['', [Validators.required, Validators.maxLength(80)]],
    address: ['', Validators.maxLength(200)],
    preferredDate: [''],
    budget: this.fb.control<number | null>(null, [Validators.min(0), Validators.max(100000)]),
  });

  ngOnInit(): void {
    this.form.patchValue({
      category: (this.category() ?? '') as ServiceCategory | '',
      city: this.city() ?? '',
      title: this.title() ?? '',
    });
  }

  protected err(name: string) {
    return controlError(this.form.get(name));
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const v = this.form.getRawValue();
    this.requests
      .create({
        ...v,
        category: v.category as ServiceCategory,
        address: v.address || null,
        preferredDate: v.preferredDate || null,
        serviceId: this.serviceId() ?? null,
      })
      .subscribe({
        next: () => this.router.navigate(['/requests'], { queryParams: { created: 1 } }),
        error: (e) => {
          this.saving.set(false);
          this.error.set(errorMessage(e));
          applyServerErrors(this.form, e);
        },
      });
  }
}
