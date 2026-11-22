import { Component, computed, inject, signal } from '@angular/core';

import { errorMessage } from '../core/api-error';
import { AccountService } from '../core/services/account.service';
import { AuthService } from '../core/services/auth.service';
import { Icon } from './icon';
import { IMAGE_ACCEPT, imageProblem, takeFile } from './image-upload';

/** Photo de profil du compte connecte : apercu, changement et suppression. */
@Component({
  selector: 'app-avatar-uploader',
  imports: [Icon],
  template: `
    <div class="uploader">
      @if (auth.user()?.avatarUrl; as url) {
        <img class="pic" [src]="url" alt="Votre photo de profil" />
      } @else {
        <span class="pic initials" aria-hidden="true">{{ initials() }}</span>
      }
      <div class="actions">
        <strong>Photo de profil</strong>
        <span class="muted small">JPEG, PNG ou WebP, 2 Mo maximum. Elle rassure vos interlocuteurs.</span>
        <div class="row">
          <label class="btn secondary small" [class.disabled]="busy()">
            <app-icon name="plus" [size]="16" /> {{ auth.user()?.avatarUrl ? 'Changer' : 'Ajouter une photo' }}
            <input type="file" class="sr-only" [accept]="accept" [disabled]="busy()" (change)="pick($event)" />
          </label>
          @if (auth.user()?.avatarUrl) {
            <button type="button" class="btn danger small" [disabled]="busy()" (click)="remove()">Supprimer</button>
          }
        </div>
        @if (error()) { <span class="error small" role="alert">{{ error() }}</span> }
      </div>
    </div>
  `,
  styles: `
    .uploader { display: flex; gap: 1.1rem; align-items: center; }
    .pic { width: 84px; height: 84px; border-radius: 50%; object-fit: cover; flex: none; border: 3px solid var(--c-surface);
      box-shadow: 0 0 0 2px var(--c-primary-soft); }
    .initials { display: grid; place-items: center; background: var(--c-primary); color: #fff; font-weight: 800; font-size: 1.6rem; }
    .actions { display: flex; flex-direction: column; gap: 0.35rem; }
    .actions label { font-weight: 700; font-size: 0.84rem; margin: 0; flex-direction: row; cursor: pointer; }
    .disabled { opacity: 0.55; pointer-events: none; }
    .error { color: var(--c-danger); font-weight: 600; }
  `,
})
export class AvatarUploader {
  protected readonly auth = inject(AuthService);
  private readonly account = inject(AccountService);
  protected readonly accept = IMAGE_ACCEPT;
  protected readonly busy = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly initials = computed(() => {
    const u = this.auth.user();
    return u ? `${u.firstName?.[0] ?? ''}${u.lastName?.[0] ?? ''}`.toUpperCase() : '';
  });

  protected pick(event: Event): void {
    const file = takeFile(event);
    if (!file) return;
    const problem = imageProblem(file);
    this.error.set(problem);
    if (problem) return;
    this.busy.set(true);
    this.account.uploadAvatar(file).subscribe({
      next: () => this.busy.set(false),
      error: (e) => {
        this.busy.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }

  protected remove(): void {
    this.busy.set(true);
    this.account.removeAvatar().subscribe({
      next: () => this.busy.set(false),
      error: (e) => {
        this.busy.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }
}
