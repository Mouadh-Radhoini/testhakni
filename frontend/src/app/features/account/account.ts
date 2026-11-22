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
import { TranslatePipe, tr } from '../../core/i18n/i18n';

/** Mon compte : informations personnelles et mot de passe (AUTH-09 / AUTH-07). FE-04 */
@Component({
  selector: 'app-account',
  imports: [ReactiveFormsModule, AvatarUploader, TranslatePipe],
  template: `
    <section class="container narrow">
      <h1>{{ 'nav.account' | t }}</h1>

      <div class="card mb">
        <app-avatar-uploader />
      </div>

      <div class="card mb">
        <h2>{{ 'ac.personal' | t }}</h2>
        <p class="muted small">
          {{ auth.user()?.email }} · {{ 'ac.accountType' | t }} <span class="badge">{{ ('role.' + auth.user()?.role) | t }}</span>
        </p>
        <form class="form" [formGroup]="profile" (ngSubmit)="saveProfile()" novalidate>
          @if (profileMsg(); as m) { <div class="alert" [class]="m.type">{{ m.text }}</div> }
          <div class="form-row">
            <div class="field">
              <label for="firstName">{{ 'auth.firstName' | t }}</label>
              <input id="firstName" formControlName="firstName" />
              @if (err(profile, 'firstName'); as e) { <span class="error">{{ e }}</span> }
            </div>
            <div class="field">
              <label for="lastName">{{ 'auth.lastName' | t }}</label>
              <input id="lastName" formControlName="lastName" />
              @if (err(profile, 'lastName'); as e) { <span class="error">{{ e }}</span> }
            </div>
          </div>
          <div class="form-row">
            <div class="field">
              <label for="phone">{{ 'auth.phone' | t }}</label>
              <input id="phone" formControlName="phone" />
              @if (err(profile, 'phone'); as e) { <span class="error">{{ e }}</span> }
            </div>
            <div class="field">
              <label for="city">{{ 'search.city' | t }}</label>
              <input id="city" formControlName="city" />
            </div>
          </div>
          <div><button class="btn" type="submit" [disabled]="saving()">{{ 'common.save' | t }}</button></div>
        </form>
      </div>

      <div class="card">
        <h2>{{ 'ac.pwdTitle' | t }}</h2>
        <form class="form" [formGroup]="password" (ngSubmit)="changePassword()" novalidate>
          @if (passwordMsg(); as m) { <div class="alert" [class]="m.type">{{ m.text }}</div> }
          <div class="field">
            <label for="currentPassword">{{ 'ac.current' | t }}</label>
            <input id="currentPassword" type="password" formControlName="currentPassword" autocomplete="current-password" />
            @if (err(password, 'currentPassword'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <div class="field">
            <label for="newPassword">{{ 'ac.new' | t }}</label>
            <input id="newPassword" type="password" formControlName="newPassword" autocomplete="new-password" />
            <span class="hint">{{ 'auth.passwordHint' | t }}</span>
            @if (err(password, 'newPassword'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <div><button class="btn secondary" type="submit">{{ 'ac.change' | t }}</button></div>
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
        this.profileMsg.set({ type: 'success', text: tr('ac.msgProfile') });
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
        this.passwordMsg.set({ type: 'success', text: tr('ac.msgPassword') });
      },
      error: (e) => {
        this.passwordMsg.set({ type: 'error', text: errorMessage(e) });
        applyServerErrors(this.password, e);
      },
    });
  }
}
