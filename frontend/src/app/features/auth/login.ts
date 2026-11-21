import { Component, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { controlError, errorMessage } from '../../core/api-error';
import { AuthService } from '../../core/services/auth.service';

/** Connexion. Redirige vers ?returnUrl ou vers l'espace adapte au role. FE-02 */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <section class="auth-shell">
      <div class="card auth-card">
        <h1>Connexion</h1>
        <form class="form" [formGroup]="form" (ngSubmit)="submit()" novalidate>
          @if (error()) {
            <div class="alert error" role="alert">{{ error() }}</div>
          }
          <div class="field">
            <label for="email">Email</label>
            <input id="email" type="email" formControlName="email" autocomplete="email" />
            @if (err('email'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <div class="field">
            <label for="password">Mot de passe</label>
            <input id="password" type="password" formControlName="password" autocomplete="current-password" />
            @if (err('password'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <button class="btn" type="submit" [disabled]="loading()">
            {{ loading() ? 'Connexion…' : 'Se connecter' }}
          </button>
          <p class="muted small">Pas encore de compte ? <a routerLink="/register">Inscrivez-vous</a></p>
        </form>
      </div>
      <aside class="auth-aside" aria-hidden="true">
        <h2>Bon retour parmi nous</h2>
        <p>Retrouvez vos demandes, vos offres et vos réservations en un clin d’œil.</p>
        <ul><li>Des prestataires de votre ville</li><li>Plusieurs offres à comparer</li><li>Des avis après chaque prestation</li></ul>
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
