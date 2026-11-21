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

/** Inscription client (AUTH-02 cote UI). FE-02 */
@Component({
  selector: 'app-register-customer',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <section class="auth-shell">
      <div class="card auth-card">
        <h1>Inscription client</h1>
        <form class="form" [formGroup]="form" (ngSubmit)="submit()" novalidate>
          @if (error()) { <div class="alert error" role="alert">{{ error() }}</div> }
          <div class="form-row">
            <div class="field">
              <label for="firstName">Prénom</label>
              <input id="firstName" formControlName="firstName" autocomplete="given-name" />
              @if (err('firstName'); as e) { <span class="error">{{ e }}</span> }
            </div>
            <div class="field">
              <label for="lastName">Nom</label>
              <input id="lastName" formControlName="lastName" autocomplete="family-name" />
              @if (err('lastName'); as e) { <span class="error">{{ e }}</span> }
            </div>
          </div>
          <div class="field">
            <label for="email">Email</label>
            <input id="email" type="email" formControlName="email" autocomplete="email" />
            @if (err('email'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <div class="field">
            <label for="password">Mot de passe</label>
            <input id="password" type="password" formControlName="password" autocomplete="new-password" />
            <span class="hint">{{ passwordHint }}</span>
            @if (err('password'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <div class="form-row">
            <div class="field">
              <label for="phone">Téléphone <span class="muted">(optionnel)</span></label>
              <input id="phone" formControlName="phone" autocomplete="tel" />
              @if (err('phone'); as e) { <span class="error">{{ e }}</span> }
            </div>
            <div class="field">
              <label for="city">Ville <span class="muted">(optionnel)</span></label>
              <input id="city" formControlName="city" />
            </div>
          </div>
          <button class="btn" type="submit" [disabled]="loading()">
            {{ loading() ? 'Création…' : 'Créer mon compte' }}
          </button>
          <p class="muted small">Vous êtes prestataire ? <a routerLink="/register/provider">Inscription prestataire</a></p>
        </form>
      </div>
      <aside class="auth-aside" aria-hidden="true">
        <h2>Trouvez le bon pro, sans stress</h2>
        <p>Publiez une demande gratuitement et recevez des offres de prestataires de votre ville.</p>
        <ul><li>Des prestataires de votre ville</li><li>Plusieurs offres à comparer</li><li>Des avis après chaque prestation</li></ul>
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
