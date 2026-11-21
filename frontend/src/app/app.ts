import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';

import { NotificationBell } from './features/marketplace/notifications';
import { AuthService } from './core/services/auth.service';
import { Icon } from './shared/icon';

const ROLE_LABELS = { CUSTOMER: 'Client', PROVIDER: 'Prestataire', ADMIN: 'Administrateur' } as const;

/** Layout principal : en-tete avec navigation selon le role (menu mobile), contenu, pied de page. FE-01 */
@Component({
  selector: 'app-root',
  imports: [NotificationBell, RouterOutlet, RouterLink, RouterLinkActive, Icon],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly auth = inject(AuthService);
  protected readonly menuOpen = signal(false);
  protected readonly year = new Date().getFullYear();

  protected readonly initials = computed(() => {
    const u = this.auth.user();
    return u ? `${u.firstName?.[0] ?? ''}${u.lastName?.[0] ?? ''}`.toUpperCase() : '';
  });
  protected readonly roleLabel = computed(() => {
    const r = this.auth.role();
    return r ? ROLE_LABELS[r] : '';
  });

  constructor() {
    // Le menu mobile se referme a chaque changement de page
    inject(Router)
      .events.pipe(filter((e) => e instanceof NavigationEnd), takeUntilDestroyed())
      .subscribe(() => this.menuOpen.set(false));
  }

  protected toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }
}
