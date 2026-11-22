import { effect, inject, Injectable } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { RouterStateSnapshot, TitleStrategy } from '@angular/router';

import { I18n, tr } from './i18n';

/**
 * Titre d'onglet traduit : "Messages · testehkni". Les routes declarent un titre francais,
 * utilise comme cle (title.<titre>) ; le titre est recalcule quand la langue change.
 */
@Injectable({ providedIn: 'root' })
export class TranslatedTitleStrategy extends TitleStrategy {
  private readonly title = inject(Title);
  private readonly i18n = inject(I18n);
  private lastRaw: string | undefined;

  constructor() {
    super();
    effect(() => {
      this.i18n.lang();
      this.render();
    });
  }

  override updateTitle(snapshot: RouterStateSnapshot): void {
    this.lastRaw = this.buildTitle(snapshot);
    this.render();
  }

  private render(): void {
    const brand = tr('brand.name');
    this.title.setTitle(this.lastRaw ? `${tr('title.' + this.lastRaw)} · ${brand}` : brand);
  }
}
