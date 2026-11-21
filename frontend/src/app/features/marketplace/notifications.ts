import { Component, effect, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { EMPTY, catchError, switchMap, timer } from 'rxjs';
import { Notice } from '../../core/marketplace.models';
import { Page } from '../../core/models';
import { AuthService } from '../../core/services/auth.service';
import { MarketplaceService } from '../../core/services/marketplace.service';
import { Feedback, PageState } from './page-state';

@Component({
  selector: 'app-notification-bell',
  imports: [RouterLink],
  template: `<a routerLink="/notifications" aria-label="Notifications"
    >🔔 Notifications
    @if (count()) {
      <span class="badge">{{ count() }}</span>
    }
  </a>`,
})
export class NotificationBell {
  private readonly auth = inject(AuthService);
  private readonly api = inject(MarketplaceService);
  readonly count = signal(0);
  constructor() {
    effect((cleanup) => {
      const user = this.auth.user();
      this.api.noticeRevision();
      this.count.set(0);
      if (!user) return;
      const sub = timer(0, 30000)
        .pipe(switchMap(() => this.api.unread().pipe(catchError(() => EMPTY))))
        .subscribe((count) => this.count.set(count));
      cleanup(() => sub.unsubscribe());
    });
  }
}

@Component({
  imports: [Feedback, DatePipe],
  template: `<section class="container">
    <h1>Notifications</h1>
    <app-feedback [state]="state" />
    @if (list(); as p) {
      @for (n of p.content; track n.id) {
        <article class="card mb">
          <p>{{ n.message }}</p>
          <p class="muted">{{ n.createdAt | date: 'dd/MM/yyyy HH:mm' }}</p>
          @if (!n.readAt) {
            <button class="btn secondary" [disabled]="state.loading()" (click)="read(n)">
              Marquer comme lue
            </button>
          } @else {
            <span>Lue</span>
          }
        </article>
      } @empty {
        <p>Aucune notification.</p>
      }
      @if (!p.first) {
        <button (click)="load(p.page - 1)">Précédent</button>
      }
      @if (!p.last) {
        <button (click)="load(p.page + 1)">Suivant</button>
      }
    }
  </section>`,
})
export class NotificationsPage implements OnInit {
  readonly state = new PageState();
  private readonly api = inject(MarketplaceService);
  readonly list = signal<Page<Notice> | null>(null);
  ngOnInit() {
    this.load(0);
  }
  load(page: number) {
    this.state.run(this.api.notices(page), (p) => this.list.set(p));
  }
  read(n: Notice) {
    this.state.run(
      this.api.read(n.id),
      () => {
        this.api.noticeRevision.update((i) => i + 1);
        this.load(this.list()?.page ?? 0);
      },
      'Notification lue.',
    );
  }
}
