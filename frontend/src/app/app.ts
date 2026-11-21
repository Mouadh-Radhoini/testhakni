import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { NotificationBell } from './features/marketplace/notifications';
import { AuthService } from './core/services/auth.service';

/** Layout principal : en-tete avec navigation selon le role, contenu, pied de page. FE-01 */
@Component({
  selector: 'app-root',
  imports: [NotificationBell, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly auth = inject(AuthService);
}
