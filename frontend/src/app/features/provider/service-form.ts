import { Component, inject, input, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { applyServerErrors, controlError, errorMessage } from '../../core/api-error';
import { PRICE_TYPE_LABELS, PriceType, ServiceCategory } from '../../core/models';
import { CatalogService } from '../../core/services/catalog.service';
import { ProviderService } from '../../core/services/provider.service';
import { TranslatePipe } from '../../core/i18n/i18n';

/** Formulaire de creation / modification d'un service (SERV-02 / SERV-03). FE-05 */
@Component({
  selector: 'app-service-form',
  imports: [ReactiveFormsModule, RouterLink, TranslatePipe],
  template: `
    <section class="container narrow">
      <p><a routerLink="/provider/services"><span class="flip">←</span> {{ 'nav.myServices' | t }}</a></p>
      <div class="card">
        <h1>{{ (id() ? 'sf.edit' : 'sf.new') | t }}</h1>
        <form class="form" [formGroup]="form" (ngSubmit)="save()" novalidate>
          @if (error()) { <div class="alert error">{{ error() }}</div> }
          <div class="field">
            <label for="title">{{ 'sf.title' | t }}</label>
            <input id="title" formControlName="title" [placeholder]="'sf.titlePh' | t" />
            @if (err('title'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <div class="field">
            <label for="description">{{ 'sf.description' | t }}</label>
            <textarea id="description" formControlName="description"></textarea>
            @if (err('description'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <div class="form-row">
            <div class="field">
              <label for="category">{{ 'search.category' | t }}</label>
              <select id="category" formControlName="category">
                <option value="" disabled>{{ 'sf.choose' | t }}</option>
                @for (c of categories(); track c.code) {
                  <option [value]="c.code">{{ ('cat.' + c.code) | t }}</option>
                }
              </select>
              @if (err('category'); as e) { <span class="error">{{ e }}</span> }
            </div>
            <div class="field">
              <label for="city">{{ 'search.city' | t }}</label>
              <input id="city" formControlName="city" />
              @if (err('city'); as e) { <span class="error">{{ e }}</span> }
            </div>
          </div>
          <div class="form-row">
            <div class="field">
              <label for="priceType">{{ 'sf.priceType' | t }}</label>
              <select id="priceType" formControlName="priceType" (change)="onPriceTypeChange()">
                @for (t of priceTypes; track t) {
                  <option [value]="t">{{ ('priceType.' + t) | t }}</option>
                }
              </select>
            </div>
            <div class="field">
              <label for="price">{{ 'sf.price' | t }}</label>
              <input id="price" type="number" min="0" step="0.5" formControlName="price" />
              @if (err('price'); as e) { <span class="error">{{ e }}</span> }
            </div>
          </div>
          <div class="row">
            <button class="btn" type="submit" [disabled]="saving()">
              {{ (id() ? 'common.save' : 'sf.publish') | t }}
            </button>
            <a class="btn secondary" routerLink="/provider/services">{{ 'common.cancel' | t }}</a>
          </div>
        </form>
      </div>
    </section>
  `,
})
export class ServiceForm implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly providers = inject(ProviderService);
  private readonly catalog = inject(CatalogService);
  private readonly router = inject(Router);

  /** Parametre :id de l'URL (absent en creation). */
  readonly id = input<string>();

  protected readonly categories = toSignal(this.catalog.categories(), { initialValue: [] });
  protected readonly priceTypes: PriceType[] = ['FIXED', 'HOURLY', 'ON_QUOTE'];
  protected readonly priceTypeLabel = PRICE_TYPE_LABELS;
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.group({
    title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(2000)]],
    category: this.fb.control<ServiceCategory | ''>('', Validators.required),
    city: ['', [Validators.required, Validators.maxLength(80)]],
    priceType: this.fb.control<PriceType>('FIXED'),
    price: this.fb.control<number | null>(null, [Validators.required, Validators.min(0), Validators.max(100000)]),
  });

  ngOnInit(): void {
    const id = this.id();
    if (!id) return;
    this.catalog.service(id).subscribe({
      next: (s) => {
        this.form.setValue({
          title: s.title,
          description: s.description,
          category: s.category,
          city: s.city,
          priceType: s.priceType ?? 'FIXED',
          price: s.price,
        });
        this.onPriceTypeChange();
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected err(name: string) {
    return controlError(this.form.get(name));
  }

  /** Le prix n'est pas demande pour un service sur devis (meme regle que le backend). */
  onPriceTypeChange(): void {
    const price = this.form.controls.price;
    if (this.form.controls.priceType.value === 'ON_QUOTE') {
      price.setValue(null);
      price.disable();
    } else {
      price.enable();
    }
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const v = this.form.getRawValue();
    const body = { ...v, category: v.category as ServiceCategory, price: v.priceType === 'ON_QUOTE' ? null : v.price };
    const id = this.id();
    const call = id ? this.providers.updateService(id, body) : this.providers.createService(body);
    call.subscribe({
      next: () => this.router.navigateByUrl('/provider/services'),
      error: (e) => {
        this.saving.set(false);
        this.error.set(errorMessage(e));
        applyServerErrors(this.form, e);
      },
    });
  }
}
