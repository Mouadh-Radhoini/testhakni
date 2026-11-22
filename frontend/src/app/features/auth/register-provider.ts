import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import {
  applyServerErrors,
  controlError,
  errorMessage,
  PASSWORD_HINT,
  PASSWORD_PATTERN,
  PHONE_PATTERN,
} from '../../core/api-error';
import { ServiceCategory } from '../../core/models';
import { AuthService } from '../../core/services/auth.service';
import { TranslatePipe } from '../../core/i18n/i18n';
import { CatalogService } from '../../core/services/catalog.service';

/** Inscription prestataire : champs pro obligatoires (AUTH-03 cote UI). FE-02 */
@Component({
  selector: 'app-register-provider',
  imports: [ReactiveFormsModule, RouterLink, TranslatePipe],
  template: `
    <section class="auth-shell">
      <div class="card auth-card">
        <h1>{{ 'auth.providerSignupTitle' | t }}</h1>
        <form class="form" [formGroup]="form" (ngSubmit)="submit()" novalidate>
          @if (error()) { <div class="alert error" role="alert">{{ error() }}</div> }
          <div class="form-row">
            <div class="field">
              <label for="firstName">{{ 'auth.firstName' | t }}</label>
              <input id="firstName" formControlName="firstName" />
              @if (err('firstName'); as e) { <span class="error">{{ e }}</span> }
            </div>
            <div class="field">
              <label for="lastName">{{ 'auth.lastName' | t }}</label>
              <input id="lastName" formControlName="lastName" />
              @if (err('lastName'); as e) { <span class="error">{{ e }}</span> }
            </div>
          </div>
          <div class="field">
            <label for="businessName">{{ 'auth.businessName' | t }}</label>
            <input id="businessName" formControlName="businessName" [placeholder]="'auth.businessPh' | t" />
            @if (err('businessName'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <div class="field">
            <label for="email">{{ 'auth.email' | t }}</label>
            <input id="email" type="email" formControlName="email" autocomplete="email" />
            @if (err('email'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <div class="field">
            <label for="password">{{ 'auth.password' | t }}</label>
            <input id="password" type="password" formControlName="password" autocomplete="new-password" />
            <span class="hint">{{ 'auth.passwordHint' | t }}</span>
            @if (err('password'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <div class="form-row">
            <div class="field">
              <label for="phone">{{ 'auth.phone' | t }}</label>
              <input id="phone" formControlName="phone" autocomplete="tel" />
              @if (err('phone'); as e) { <span class="error">{{ e }}</span> }
            </div>
            <div class="field">
              <label for="city">{{ 'auth.city' | t }}</label>
              <input id="city" formControlName="city" />
              @if (err('city'); as e) { <span class="error">{{ e }}</span> }
            </div>
          </div>
          <fieldset class="field">
            <label>{{ 'auth.categories' | t }}</label>
            <div class="checks">
              @for (c of categories(); track c.code) {
                <label>
                  <input type="checkbox" [checked]="isSelected(c.code)" (change)="toggle(c.code)" />
                  {{ ('cat.' + c.code) | t }}
                </label>
              }
            </div>
            @if (err('categories'); as e) { <span class="error">{{ e }}</span> }
          </fieldset>
          <button class="btn" type="submit" [disabled]="loading()">
            {{ (loading() ? 'auth.creating' : 'auth.createPro') | t }}
          </button>
          <p class="muted small">{{ 'auth.lookingService' | t }} <a routerLink="/register/customer">{{ 'auth.customerSignupTitle' | t }}</a></p>
        </form>
      </div>
      <aside class="auth-aside" aria-hidden="true">
        <h2>{{ 'auth.providerAsideTitle' | t }}</h2>
        <p>{{ 'auth.providerAside' | t }}</p>
        <ul><li>{{ 'auth.bullet1' | t }}</li><li>{{ 'auth.bullet2' | t }}</li><li>{{ 'auth.bullet3' | t }}</li></ul>
      </aside>
    </section>
  `,
  styles: `fieldset { border: 0; padding: 0; margin: 0; }`,
})
export class RegisterProviderPage {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly categories = toSignal(inject(CatalogService).categories(), { initialValue: [] });
  protected readonly passwordHint = PASSWORD_HINT;
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.group({
    firstName: ['', [Validators.required, Validators.maxLength(50)]],
    lastName: ['', [Validators.required, Validators.maxLength(50)]],
    businessName: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]],
    phone: ['', [Validators.required, Validators.pattern(PHONE_PATTERN)]],
    city: ['', [Validators.required, Validators.maxLength(80)]],
    categories: this.fb.control<ServiceCategory[]>([], Validators.required),
  });

  protected err(name: string) {
    return controlError(this.form.get(name));
  }

  protected isSelected(code: ServiceCategory): boolean {
    return this.form.controls.categories.value.includes(code);
  }

  protected toggle(code: ServiceCategory): void {
    const ctrl = this.form.controls.categories;
    const value = ctrl.value.includes(code) ? ctrl.value.filter((c) => c !== code) : [...ctrl.value, code];
    ctrl.setValue(value);
    ctrl.markAsTouched();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.auth.registerProvider(this.form.getRawValue()).subscribe({
      next: () => this.router.navigateByUrl('/provider/profile'),
      error: (e) => {
        this.loading.set(false);
        this.error.set(errorMessage(e));
        applyServerErrors(this.form, e);
      },
    });
  }
}
