import { Component, inject, signal } from '@angular/core';
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
import { AuthService } from '../../core/services/auth.service';
import { TranslatePipe } from '../../core/i18n/i18n';

/** Inscription client (AUTH-02 cote UI). FE-02 */
@Component({
  selector: 'app-register-customer',
  imports: [ReactiveFormsModule, RouterLink, TranslatePipe],
  template: `
    <section class="auth-shell">
      <div class="card auth-card">
        <h1>{{ 'auth.customerSignupTitle' | t }}</h1>
        <form class="form" [formGroup]="form" (ngSubmit)="submit()" novalidate>
          @if (error()) { <div class="alert error" role="alert">{{ error() }}</div> }
          <div class="form-row">
            <div class="field">
              <label for="firstName">{{ 'auth.firstName' | t }}</label>
              <input id="firstName" formControlName="firstName" autocomplete="given-name" />
              @if (err('firstName'); as e) { <span class="error">{{ e }}</span> }
            </div>
            <div class="field">
              <label for="lastName">{{ 'auth.lastName' | t }}</label>
              <input id="lastName" formControlName="lastName" autocomplete="family-name" />
              @if (err('lastName'); as e) { <span class="error">{{ e }}</span> }
            </div>
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
              <label for="phone">{{ 'auth.phone' | t }} <span class="muted">{{ 'auth.optional' | t }}</span></label>
              <input id="phone" formControlName="phone" autocomplete="tel" />
              @if (err('phone'); as e) { <span class="error">{{ e }}</span> }
            </div>
            <div class="field">
              <label for="city">{{ 'auth.city' | t }} <span class="muted">{{ 'auth.optional' | t }}</span></label>
              <input id="city" formControlName="city" />
            </div>
          </div>
          <button class="btn" type="submit" [disabled]="loading()">
            {{ (loading() ? 'auth.creating' : 'auth.create') | t }}
          </button>
          <p class="muted small">{{ 'auth.areProvider' | t }} <a routerLink="/register/provider">{{ 'auth.providerSignupTitle' | t }}</a></p>
        </form>
      </div>
      <aside class="auth-aside" aria-hidden="true">
        <h2>{{ 'auth.customerAsideTitle' | t }}</h2>
        <p>{{ 'auth.customerAside' | t }}</p>
        <ul><li>{{ 'auth.bullet1' | t }}</li><li>{{ 'auth.bullet2' | t }}</li><li>{{ 'auth.bullet3' | t }}</li></ul>
      </aside>
    </section>
  `,
})
export class RegisterCustomerPage {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly passwordHint = PASSWORD_HINT;
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.group({
    firstName: ['', [Validators.required, Validators.maxLength(50)]],
    lastName: ['', [Validators.required, Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]],
    phone: ['', Validators.pattern(PHONE_PATTERN)],
    city: ['', Validators.maxLength(80)],
  });

  protected err(name: string) {
    return controlError(this.form.get(name));
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    const v = this.form.getRawValue();
    this.auth.registerCustomer({ ...v, phone: v.phone || null, city: v.city || null }).subscribe({
      next: () => this.router.navigateByUrl('/services'),
      error: (e) => {
        this.loading.set(false);
        this.error.set(errorMessage(e));
        applyServerErrors(this.form, e);
      },
    });
  }
}
