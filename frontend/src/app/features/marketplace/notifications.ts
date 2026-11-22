import { Component, effect, inject, OnInit, signal } from '@angular/core';
import { Icon } from '../../shared/icon';
import { tr, TranslatePipe } from '../../core/i18n/i18n';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { EMPTY, catchError, switchMap, timer } from 'rxjs';
import { Notice } from '../../core/marketplace.models';
import { Page } from '../../core/models';
import { AuthService } from '../../core/services/auth.service';
import { MarketplaceService } from '../../core/services/marketplace.service';
import { ServerTextPipe } from '../../core/i18n/server-messages';
import { Feedback, PageState } from './page-state';

@Component({
  selector: 'app-notification-bell',
  imports: [RouterLink, Icon],
  template: `<a routerLink="/notifications" class="bell" [attr.aria-label]="label()" [attr.title]="label()">
    <app-icon name="bell" />
    @if (count()) {
      <span class="dot">{{ count() > 9 ? '9+' : count() }}</span>
    }
  </a>`,
  styles: `
    .bell { position: relative; display: inline-grid; place-items: center; width: 40px; height: 40px;
      border-radius: 50%; color: var(--c-text); }
    .bell:hover { background: rgba(23, 32, 29, 0.06); color: var(--c-text); }
    .dot { position: absolute; top: 3px; inset-inline-end: 2px; min-width: 18px; height: 18px; padding: 0 4px;
      border-radius: 999px; background: var(--c-accent); color: #2b1a00; font-size: 0.68rem; font-weight: 800;
      display: grid; place-items: center; border: 2px solid var(--c-surface); }
  `,
})
export class NotificationBell {
  protected readonly label = () =>
    this.count() ? tr('nav.notificationsUnread', { n: this.count() }) : tr('nav.notifications');
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
  imports: [Feedback, DatePipe, TranslatePipe, ServerTextPipe],
  template: `<section class="container">
    <h1>{{ 'nav.notifications' | t }}</h1>
    <app-feedback [state]="state" />
    @if (list(); as p) {
      @for (n of p.content; track n.id) {
        <article class="card mb">
          <p>{{ n.message | serverText }}</p>
          <p class="muted">{{ n.createdAt | date: 'dd/MM/yyyy HH:mm' }}</p>
          @if (!n.readAt) {
            <button class="btn secondary" [disabled]="state.loading()" (click)="read(n)">
              {{ 'nt.markRead' | t }}
            </button>
          } @else {
            <span>{{ 'nt.read' | t }}</span>
          }
        </article>
      } @empty {
        <p>{{ 'nt.none' | t }}</p>
      }
      @if (!p.first) {
        <button (click)="load(p.page - 1)">{{ 'search.prev' | t }}</button>
      }
      @if (!p.last) {
        <button (click)="load(p.page + 1)">{{ 'search.next' | t }}</button>
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
      tr('nt.msgRead'),
    );
  }
}
