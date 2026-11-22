import { Component, inject, OnInit, signal } from '@angular/core';
import { FormGroup, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import {
  applyServerErrors,
  controlError,
  errorMessage,
  PASSWORD_HINT,
  PASSWORD_PATTERN,
  PHONE_PATTERN,
} from '../../core/api-error';
import { AccountService } from '../../core/services/account.service';
import { AuthService } from '../../core/services/auth.service';
import { AvatarUploader } from '../../shared/avatar-uploader';

/** Mon compte : informations personnelles et mot de passe (AUTH-09 / AUTH-07). FE-04 */
@Component({
  selector: 'app-account',
  imports: [ReactiveFormsModule, AvatarUploader],
  template: `
    <section class="container narrow">
      <h1>Mon compte</h1>

      <div class="card mb">
        <app-avatar-uploader />
      </div>

      <div class="card mb">
        <h2>Informations personnelles</h2>
        <p class="muted small">
          {{ auth.user()?.email }} · compte <span class="badge">{{ auth.user()?.role }}</span>
        </p>
        <form class="form" [formGroup]="profile" (ngSubmit)="saveProfile()" novalidate>
          @if (profileMsg(); as m) { <div class="alert" [class]="m.type">{{ m.text }}</div> }
          <div class="form-row">
            <div class="field">
              <label for="firstName">Prénom</label>
              <input id="firstName" formControlName="firstName" />
              @if (err(profile, 'firstName'); as e) { <span class="error">{{ e }}</span> }
            </div>
            <div class="field">
              <label for="lastName">Nom</label>
              <input id="lastName" formControlName="lastName" />
              @if (err(profile, 'lastName'); as e) { <span class="error">{{ e }}</span> }
            </div>
          </div>
          <div class="form-row">
            <div class="field">
              <label for="phone">Téléphone</label>
              <input id="phone" formControlName="phone" />
              @if (err(profile, 'phone'); as e) { <span class="error">{{ e }}</span> }
            </div>
            <div class="field">
              <label for="city">Ville</label>
              <input id="city" formControlName="city" />
            </div>
          </div>
          <div><button class="btn" type="submit" [disabled]="saving()">Enregistrer</button></div>
        </form>
      </div>

      <div class="card">
        <h2>Changer de mot de passe</h2>
        <form class="form" [formGroup]="password" (ngSubmit)="changePassword()" novalidate>
          @if (passwordMsg(); as m) { <div class="alert" [class]="m.type">{{ m.text }}</div> }
          <div class="field">
            <label for="currentPassword">Mot de passe actuel</label>
            <input id="currentPassword" type="password" formControlName="currentPassword" autocomplete="current-password" />
            @if (err(password, 'currentPassword'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <div class="field">
            <label for="newPassword">Nouveau mot de passe</label>
            <input id="newPassword" type="password" formControlName="newPassword" autocomplete="new-password" />
            <span class="hint">{{ passwordHint }}</span>
            @if (err(password, 'newPassword'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <div><button class="btn secondary" type="submit">Modifier le mot de passe</button></div>
        </form>
      </div>
    </section>
  `,
})
export class Account implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly account = inject(AccountService);
  protected readonly auth = inject(AuthService);

  protected readonly passwordHint = PASSWORD_HINT;
  protected readonly saving = signal(false);
  protected readonly profileMsg = signal<{ type: string; text: string } | null>(null);
  protected readonly passwordMsg = signal<{ type: string; text: string } | null>(null);

  protected readonly profile = this.fb.group({
    firstName: ['', [Validators.required, Validators.maxLength(50)]],
    lastName: ['', [Validators.required, Validators.maxLength(50)]],
    phone: ['', Validators.pattern(PHONE_PATTERN)],
    city: ['', Validators.maxLength(80)],
  });

  protected readonly password = this.fb.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]],
  });

  ngOnInit(): void {
    this.account.me().subscribe({
      next: (u) =>
        this.profile.setValue({
          firstName: u.firstName,
          lastName: u.lastName,
          phone: u.phone ?? '',
          city: u.city ?? '',
        }),
      error: (e) => this.profileMsg.set({ type: 'error', text: errorMessage(e) }),
    });
  }

  protected err(form: FormGroup, name: string) {
    return controlError(form.get(name));
  }

  saveProfile(): void {
    if (this.profile.invalid) {
      this.profile.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const v = this.profile.getRawValue();
    this.account.update({ ...v, phone: v.phone || null, city: v.city || null }).subscribe({
      next: () => {
        this.saving.set(false);
        this.profileMsg.set({ type: 'success', text: 'Profil mis à jour.' });
      },
      error: (e) => {
        this.saving.set(false);
        this.profileMsg.set({ type: 'error', text: errorMessage(e) });
        applyServerErrors(this.profile, e);
      },
    });
  }

  changePassword(): void {
    if (this.password.invalid) {
      this.password.markAllAsTouched();
      return;
    }
    const { currentPassword, newPassword } = this.password.getRawValue();
    this.account.changePassword(currentPassword, newPassword).subscribe({
      next: () => {
        this.password.reset();
        this.passwordMsg.set({ type: 'success', text: 'Mot de passe modifié.' });
      },
      error: (e) => {
        this.passwordMsg.set({ type: 'error', text: errorMessage(e) });
        applyServerErrors(this.password, e);
      },
    });
  }
}
