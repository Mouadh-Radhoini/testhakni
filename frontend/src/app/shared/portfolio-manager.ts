import { Component, inject, model, signal } from '@angular/core';

import { errorMessage } from '../core/api-error';
import { ProviderService } from '../core/services/provider.service';
import { Icon } from './icon';
import { IMAGE_ACCEPT, imageProblem, takeFile } from './image-upload';

/** Galerie de realisations du prestataire connecte : ajout (6 max) et suppression. */
@Component({
  selector: 'app-portfolio-manager',
  imports: [Icon],
  template: `
    <div class="gallery">
      @for (url of photos(); track url) {
        <figure>
          <img [src]="url" alt="Réalisation" loading="lazy" />
          <button type="button" class="remove" [disabled]="busy()" (click)="remove(url)" aria-label="Supprimer cette photo">
            <app-icon name="close" [size]="16" />
          </button>
        </figure>
      }
      @if (photos().length < max) {
        <label class="add" [class.disabled]="busy()">
          <app-icon name="plus" [size]="26" />
          <span>{{ busy() ? 'Envoi…' : 'Ajouter une photo' }}</span>
          <input type="file" class="sr-only" [accept]="accept" [disabled]="busy()" (change)="pick($event)" />
        </label>
      }
    </div>
    <p class="muted small">{{ photos().length }} / {{ max }} photos · JPEG, PNG ou WebP, 2 Mo maximum.</p>
    @if (error()) { <div class="alert error" role="alert">{{ error() }}</div> }
  `,
  styles: `
    .gallery { display: grid; gap: 0.75rem; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); }
    figure { position: relative; margin: 0; aspect-ratio: 4 / 3; border-radius: 12px; overflow: hidden; background: var(--c-surface-2); }
    img { width: 100%; height: 100%; object-fit: cover; }
    .remove { position: absolute; top: 6px; right: 6px; width: 30px; height: 30px; border-radius: 50%; border: 0;
      background: rgba(16, 35, 30, 0.75); color: #fff; display: grid; place-items: center; cursor: pointer; }
    .remove:hover { background: var(--c-danger); }
    .add { aspect-ratio: 4 / 3; border: 2px dashed var(--c-border-strong); border-radius: 12px; display: flex; flex-direction: column;
      align-items: center; justify-content: center; gap: 0.35rem; color: var(--c-primary); font-weight: 700; font-size: 0.85rem;
      cursor: pointer; margin: 0; background: var(--c-surface-2); }
    .add:hover { border-color: var(--c-primary); background: var(--c-primary-tint); }
    .disabled { opacity: 0.55; pointer-events: none; }
  `,
})
export class PortfolioManager {
  private readonly providers = inject(ProviderService);
  /** URLs des photos, mises a jour apres chaque envoi ou suppression. */
  readonly photos = model<string[]>([]);
  protected readonly max = 6;
  protected readonly accept = IMAGE_ACCEPT;
  protected readonly busy = signal(false);
  protected readonly error = signal<string | null>(null);

  protected pick(event: Event): void {
    const file = takeFile(event);
    if (!file) return;
    const problem = imageProblem(file);
    this.error.set(problem);
    if (problem) return;
    this.busy.set(true);
    this.providers.addPhoto(file).subscribe({
      next: (p) => this.done(p.photoUrls ?? []),
      error: (e) => this.fail(e),
    });
  }

  protected remove(url: string): void {
    this.busy.set(true);
    this.providers.removePhoto(url).subscribe({
      next: (p) => this.done(p.photoUrls ?? []),
      error: (e) => this.fail(e),
    });
  }

  private done(urls: string[]): void {
    this.busy.set(false);
    this.error.set(null);
    this.photos.set(urls);
  }

  private fail(e: unknown): void {
    this.busy.set(false);
    this.error.set(errorMessage(e));
  }
}
