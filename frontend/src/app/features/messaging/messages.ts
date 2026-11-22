import { DatePipe } from '@angular/common';
import { Component, computed, effect, ElementRef, inject, input, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { errorMessage } from '../../core/api-error';
import { AuthService } from '../../core/services/auth.service';
import { ChatMessage, Conversation, MessagingService } from '../../core/services/messaging.service';
import { Icon } from '../../shared/icon';
import { TranslatePipe } from '../../core/i18n/i18n';
import { LocalDatePipe } from '../../shared/local-date.pipe';

/** Messagerie : liste des conversations et fil de discussion en temps reel. */
@Component({
  selector: 'app-messages',
  imports: [RouterLink, RouterLinkActive, FormsModule, DatePipe, Icon, TranslatePipe, LocalDatePipe],
  template: `
    <section class="container">
      <div class="page-head">
        <div>
          <h1>{{ 'chat.title' | t }}</h1>
          <p>{{ (auth.role() === 'PROVIDER' ? 'chat.subProvider' : 'chat.subCustomer') | t }}</p>
        </div>
        <span class="spacer"></span>
        <span class="live" [class.on]="messaging.connected()">
          <span class="dot"></span>{{ (messaging.connected() ? 'chat.live' : 'chat.reconnecting') | t }}
        </span>
      </div>

      @if (error()) { <div class="alert error" role="alert">{{ error() }}</div> }

      <div class="chat" [class.has-thread]="!!id()">
        <aside class="inbox card" [attr.aria-label]="'chat.conversations' | t">
          @for (c of conversations(); track c.id) {
            <a class="conv" [routerLink]="['/messages', c.id]" routerLinkActive="active">
              @if (c.otherParticipantAvatarUrl) {
                <img class="avatar img" [src]="c.otherParticipantAvatarUrl" alt="" />
              } @else {
                <span class="avatar">{{ initials(c.otherParticipantName) }}</span>
              }
              <span class="conv-text">
                <span class="conv-top">
                  <strong>{{ c.otherParticipantName }}</strong>
                  @if (c.lastMessageAt) { <small>{{ c.lastMessageAt | localDate: 'dd/MM HH:mm' }}</small> }
                </span>
                <span class="conv-title">{{ c.title || ('chat.direct' | t) }}</span>
                <span class="conv-preview">{{ c.lastMessagePreview || ('chat.noMessage' | t) }}</span>
              </span>
              @if (c.unread) { <span class="count">{{ c.unread }}</span> }
            </a>
          } @empty {
            <div class="empty-inbox">
              <app-icon name="chat" [size]="28" />
              <p>{{ 'chat.empty' | t }}</p>
              <p class="small muted">
                {{ (auth.role() === 'PROVIDER' ? 'chat.emptyProvider' : 'chat.emptyCustomer') | t }}
              </p>
            </div>
          }
        </aside>

        <div class="thread card">
          @if (current(); as c) {
            <header class="thread-head">
              <a class="back btn ghost small" routerLink="/messages" [attr.aria-label]="'chat.back' | t"><span class="flip">←</span></a>
              <div>
                <strong>{{ c.otherParticipantName }}</strong>
                <small>{{ c.title || ('chat.direct' | t) }}</small>
              </div>
            </header>

            <div class="scroll" #scroller>
              @if (hasMore()) {
                <button class="btn ghost small more" type="button" (click)="loadOlder()">{{ 'chat.older' | t }}</button>
              }
              @for (m of messages(); track m.id) {
                <div class="bubble" [class.mine]="m.senderId === auth.user()?.id">
                  <p>{{ m.body }}</p>
                  <time>{{ m.sentAt | localDate: 'dd/MM HH:mm' }}</time>
                </div>
              } @empty {
                <p class="muted small hint">{{ 'chat.first' | t }}</p>
              }
            </div>

            <form class="composer" (ngSubmit)="send()">
              <label class="sr-only" for="msg">{{ 'chat.yourMessage' | t }}</label>
              <textarea id="msg" name="msg" rows="1" maxlength="2000" [(ngModel)]="draft"
                        [placeholder]="'chat.placeholder' | t" (keydown.enter)="onEnter($event)"></textarea>
              <button class="btn" type="submit" [disabled]="!draft.trim() || sending()" [attr.aria-label]="'chat.send' | t">
                <app-icon name="arrow" [size]="18" class="flip" />
              </button>
            </form>
          } @else {
            <div class="placeholder">
              <span class="icon-tile"><app-icon name="chat" [size]="26" /></span>
              <p>{{ 'chat.pick' | t }}</p>
            </div>
          }
        </div>
      </div>
    </section>
  `,
  styles: `
    .live { display: inline-flex; align-items: center; gap: 0.4rem; font-size: 0.82rem; font-weight: 700; color: var(--c-muted); }
    .live .dot { width: 8px; height: 8px; border-radius: 50%; background: #c3beb0; }
    .live.on { color: var(--c-success); }
    .live.on .dot { background: var(--c-success); box-shadow: 0 0 0 4px var(--c-success-soft); }

    .chat { display: grid; grid-template-columns: 340px 1fr; gap: 1rem; height: min(72vh, 720px); }
    .inbox { padding: 0.4rem; overflow-y: auto; }
    .conv { display: flex; gap: 0.7rem; align-items: flex-start; padding: 0.75rem; border-radius: 12px; color: inherit; font-weight: inherit; }
    .conv:hover { background: var(--c-surface-2); text-decoration: none; color: inherit; }
    .conv.active { background: var(--c-primary-tint); }
    .img { object-fit: cover; padding: 0; }
    .conv-text { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 0.1rem; }
    .conv-top { display: flex; justify-content: space-between; gap: 0.5rem; }
    .conv-top small { color: var(--c-muted); font-size: 0.75rem; white-space: nowrap; }
    .conv-title { font-size: 0.8rem; color: var(--c-primary); font-weight: 650; }
    .conv-title, .conv-preview { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .conv-preview { font-size: 0.85rem; color: var(--c-muted); }
    .count { align-self: center; min-width: 22px; height: 22px; padding: 0 6px; border-radius: 999px;
      background: var(--c-accent); color: #2b1a00; font-size: 0.75rem; font-weight: 800; display: grid; place-items: center; }
    .empty-inbox { text-align: center; padding: 2.5rem 1rem; color: var(--c-muted); }
    .empty-inbox app-icon { margin: 0 auto 0.5rem; color: var(--c-primary); }

    .thread { display: flex; flex-direction: column; padding: 0; overflow: hidden; }
    .thread-head { display: flex; align-items: center; gap: 0.6rem; padding: 0.9rem 1.2rem; border-bottom: 1px solid var(--c-border); }
    .thread-head div { display: flex; flex-direction: column; }
    .thread-head small { color: var(--c-muted); }
    .back { display: none; }
    .scroll { flex: 1; overflow-y: auto; padding: 1.2rem; display: flex; flex-direction: column; gap: 0.5rem; background: var(--c-surface-2); }
    .more { align-self: center; }
    .hint { text-align: center; margin: auto; max-width: 320px; }
    .bubble { max-width: min(75%, 520px); padding: 0.6rem 0.85rem; border-radius: 16px; border-end-start-radius: 4px;
      background: var(--c-surface); border: 1px solid var(--c-border); align-self: flex-start; }
    .bubble.mine { align-self: flex-end; background: var(--c-primary); color: #fff; border-color: var(--c-primary);
      border-radius: 16px; border-end-end-radius: 4px; }
    .bubble p { margin: 0; white-space: pre-wrap; overflow-wrap: anywhere; }
    .bubble time { display: block; font-size: 0.7rem; opacity: 0.7; margin-top: 0.2rem; text-align: end; }
    .composer { display: flex; gap: 0.5rem; padding: 0.8rem; border-top: 1px solid var(--c-border); background: var(--c-surface); }
    .composer textarea { min-height: 44px; max-height: 140px; resize: none; border-radius: 22px; padding: 0.65rem 1rem; }
    .composer .btn { width: 46px; height: 46px; padding: 0; flex: none; }
    .placeholder { margin: auto; text-align: center; color: var(--c-muted); }
    .placeholder .icon-tile { margin: 0 auto 0.75rem; }

    @media (max-width: 820px) {
      .chat { grid-template-columns: 1fr; height: auto; }
      .chat.has-thread .inbox { display: none; }
      .chat:not(.has-thread) .thread { display: none; }
      .thread { height: 75vh; }
      .back { display: inline-flex; }
    }
  `,
})
export class MessagesPage {
  protected readonly auth = inject(AuthService);
  protected readonly messaging = inject(MessagingService);

  /** Lu depuis /messages/:id grace a withComponentInputBinding. */
  readonly id = input<string>();

  protected readonly conversations = signal<Conversation[]>([]);
  protected readonly messages = signal<ChatMessage[]>([]);
  protected readonly hasMore = signal(false);
  protected readonly sending = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly current = computed(() => this.conversations().find((c) => c.id === this.id()) ?? null);
  protected draft = '';

  private readonly scroller = viewChild<ElementRef<HTMLElement>>('scroller');

  constructor() {
    this.loadConversations();

    // Changement de fil : charger l'historique et le marquer comme lu
    effect(() => {
      const id = this.id();
      this.messages.set([]);
      if (!id) return;
      this.messaging.messages(id).subscribe({
        next: (list) => {
          this.messages.set(list);
          this.hasMore.set(list.length === 50);
          this.scrollToEnd();
          this.markRead(id);
        },
        error: (e) => this.error.set(errorMessage(e)),
      });
    });

    // Message recu en direct
    this.messaging.incoming.pipe(takeUntilDestroyed()).subscribe((m) => {
      if (m.conversationId === this.id()) {
        if (!this.messages().some((x) => x.id === m.id)) {
          this.messages.update((list) => [...list, m]);
          this.scrollToEnd();
        }
        if (m.senderId !== this.auth.user()?.id) this.markRead(m.conversationId);
      }
      this.loadConversations();
    });
  }

  protected initials(name: string): string {
    return name.split(' ').map((p) => p[0] ?? '').join('').slice(0, 2).toUpperCase();
  }

  protected onEnter(event: Event): void {
    const e = event as KeyboardEvent;
    if (!e.shiftKey) {
      e.preventDefault();
      this.send();
    }
  }

  protected send(): void {
    const id = this.id();
    const body = this.draft.trim();
    if (!id || !body || this.sending()) return;
    this.sending.set(true);
    this.messaging.send(id, body).subscribe({
      next: (m) => {
        this.sending.set(false);
        this.draft = '';
        if (!this.messages().some((x) => x.id === m.id)) this.messages.update((list) => [...list, m]);
        this.scrollToEnd();
        this.loadConversations();
      },
      error: (e) => {
        this.sending.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }

  protected loadOlder(): void {
    const id = this.id();
    const first = this.messages()[0];
    if (!id || !first) return;
    this.messaging.messages(id, first.sentAt).subscribe((older) => {
      this.hasMore.set(older.length === 50);
      this.messages.update((list) => [...older, ...list]);
    });
  }

  private loadConversations(): void {
    this.messaging.list().subscribe({
      next: (list) => this.conversations.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private markRead(id: string): void {
    this.messaging.markRead(id).subscribe({
      next: () => this.conversations.update((list) => list.map((c) => (c.id === id ? { ...c, unread: 0 } : c))),
      error: () => undefined,
    });
  }

  private scrollToEnd(): void {
    setTimeout(() => {
      const el = this.scroller()?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    });
  }
}
