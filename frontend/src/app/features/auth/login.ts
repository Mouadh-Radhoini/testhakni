import { Component, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { controlError, errorMessage } from '../../core/api-error';
import { AuthService } from '../../core/services/auth.service';
import { TranslatePipe } from '../../core/i18n/i18n';

/** Connexion. Redirige vers ?returnUrl ou vers l'espace adapte au role. FE-02 */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, TranslatePipe],
  template: `
    <section class="auth-shell">
      <div class="card auth-card">
        <h1>{{ 'auth.loginTitle' | t }}</h1>
        <form class="form" [formGroup]="form" (ngSubmit)="submit()" novalidate>
          @if (error()) {
            <div class="alert error" role="alert">{{ error() }}</div>
          }
          <div class="field">
            <label for="email">{{ 'auth.email' | t }}</label>
            <input id="email" type="email" formControlName="email" autocomplete="email" />
            @if (err('email'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <div class="field">
            <label for="password">{{ 'auth.password' | t }}</label>
            <input id="password" type="password" formControlName="password" autocomplete="current-password" />
            @if (err('password'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <button class="btn" type="submit" [disabled]="loading()">
            {{ (loading() ? 'auth.signingIn' : 'auth.signIn') | t }}
          </button>
          <p class="muted small">{{ 'auth.noAccount' | t }} <a routerLink="/register">{{ 'auth.signUp' | t }}</a></p>
        </form>
      </div>
      <aside class="auth-aside" aria-hidden="true">
        <h2>{{ 'auth.loginAsideTitle' | t }}</h2>
        <p>{{ 'auth.loginAside' | t }}</p>
        <ul><li>{{ 'auth.bullet1' | t }}</li><li>{{ 'auth.bullet2' | t }}</li><li>{{ 'auth.bullet3' | t }}</li></ul>
      </aside>
    </section>
  `,
})
export class Login {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  /** Lu depuis ?returnUrl=... grace a withComponentInputBinding. */
  readonly returnUrl = input<string>();

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
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
    const { email, password } = this.form.getRawValue();
    this.auth.login(email, password).subscribe({
      next: (res) => {
        this.loading.set(false);
        const fallback = ({ADMIN: '/admin/dashboard', PROVIDER: '/provider/services', CUSTOMER: '/services'})[res.user.role];
        this.router.navigateByUrl(this.returnUrl() || fallback);
      },
      error: (e) => {
        this.loading.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }
}
