import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';

import { I18n, TranslatePipe } from './core/i18n/i18n';
import { AuthService } from './core/services/auth.service';
import { ContactService } from './core/services/contact.service';
import { MessagingService } from './core/services/messaging.service';
import { ThemeService } from './core/theme';
import { NotificationBell } from './features/marketplace/notifications';
import { Icon } from './shared/icon';

/**
 * Layout principal : en-tete (navigation selon le role, langue, theme, menu compte, menu mobile),
 * contenu et pied de page. FE-01
 */
@Component({
  selector: 'app-root',
  imports: [NotificationBell, RouterOutlet, RouterLink, RouterLinkActive, Icon, TranslatePipe, NgTemplateOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly auth = inject(AuthService);
  protected readonly messaging = inject(MessagingService);
  protected readonly contacts = inject(ContactService);
  protected readonly i18n = inject(I18n);
  protected readonly theme = inject(ThemeService);
  protected readonly menuOpen = signal(false);
  protected readonly userMenu = signal(false);
  protected readonly year = new Date().getFullYear();

  protected readonly initials = computed(() => {
    const u = this.auth.user();
    return u ? `${u.firstName?.[0] ?? ''}${u.lastName?.[0] ?? ''}`.toUpperCase() : '';
  });

  constructor() {
    // Les menus se referment a chaque changement de page
    inject(Router)
      .events.pipe(filter((e) => e instanceof NavigationEnd), takeUntilDestroyed())
      .subscribe(() => {
        this.menuOpen.set(false);
        this.userMenu.set(false);
      });
  }

  protected toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  protected toggleUserMenu(): void {
    this.userMenu.update((open) => !open);
  }

  /** Ferme le menu compte quand le focus le quitte (clic ailleurs, tabulation). */
  protected onUserBlur(event: FocusEvent): void {
    const next = event.relatedTarget as Node | null;
    if (!next || !(event.currentTarget as HTMLElement).contains(next)) this.userMenu.set(false);
  }
}
