import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { errorMessage } from '../../core/api-error';
import { TranslatePipe } from '../../core/i18n/i18n';
import { Contact, ContactService } from '../../core/services/contact.service';
import { MessagingService } from '../../core/services/messaging.service';
import { Icon } from '../../shared/icon';

type Tab = 'contacts' | 'incoming' | 'outgoing';

/** Mes contacts : liste, demandes recues (accepter / refuser) et demandes envoyees. */
@Component({
  selector: 'app-contacts',
  imports: [RouterLink, Icon, TranslatePipe],
  template: `
    <section class="container">
      <div class="page-head">
        <div>
          <h1>{{ 'contact.title' | t }}</h1>
          <p>{{ 'contact.sub' | t }}</p>
        </div>
      </div>

      @if (error()) { <div class="alert error" role="alert">{{ error() }}</div> }

      <nav class="tabs">
        <button type="button" [class.active]="tab() === 'contacts'" (click)="tab.set('contacts')">
          {{ 'contact.tabContacts' | t: { n: accepted().length } }}
        </button>
        <button type="button" [class.active]="tab() === 'incoming'" (click)="tab.set('incoming')">
          {{ 'contact.tabIncoming' | t: { n: incoming().length } }}
        </button>
        <button type="button" [class.active]="tab() === 'outgoing'" (click)="tab.set('outgoing')">
          {{ 'contact.tabOutgoing' | t: { n: outgoing().length } }}
        </button>
      </nav>

      <div class="list-grid">
        @for (c of shown(); track c.id) {
          <article class="card person">
            @if (c.otherAvatarUrl) {
              <img class="avatar big photo" [src]="c.otherAvatarUrl" alt="" />
            } @else {
              <span class="avatar big">{{ initials(c.otherName) }}</span>
            }
            <div class="who">
              @if (c.otherRole === 'PROVIDER') {
                <a [routerLink]="['/providers', c.otherId]"><strong>{{ c.otherName }}</strong></a>
              } @else {
                <strong>{{ c.otherName }}</strong>
              }
              <span class="small muted">{{ ('role.' + c.otherRole) | t }}</span>
            </div>
            <div class="row actions">
              @switch (tab()) {
                @case ('contacts') {
                  <button type="button" class="btn small" (click)="message(c)"><app-icon name="chat" [size]="16" /> {{ 'contact.message' | t }}</button>
                  <button type="button" class="btn ghost small" (click)="remove(c)">{{ 'contact.remove' | t }}</button>
                }
                @case ('incoming') {
                  <button type="button" class="btn small" (click)="accept(c)">{{ 'contact.accept' | t }}</button>
                  <button type="button" class="btn ghost small" (click)="decline(c)">{{ 'contact.decline' | t }}</button>
                }
                @case ('outgoing') {
                  @if (c.status === 'DECLINED') {
                    <span class="badge DISMISSED">{{ 'contact.declined' | t }}</span>
                  } @else {
                    <span class="badge SUBMITTED">{{ 'contact.pendingOut' | t }}</span>
                  }
                  <button type="button" class="btn ghost small" (click)="remove(c)">{{ 'contact.cancel' | t }}</button>
                }
              }
            </div>
          </article>
        } @empty {
          <div class="card empty">
            {{ (tab() === 'contacts' ? 'contact.empty' : tab() === 'incoming' ? 'contact.emptyIncoming' : 'contact.emptyOutgoing') | t }}
          </div>
        }
      </div>
    </section>
  `,
  styles: `
    .list-grid { display: grid; gap: 0.9rem; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); }
    .person { display: flex; align-items: center; gap: 0.9rem; flex-wrap: wrap; margin: 0; }
    .person + .person { margin-top: 0; }
    .big { width: 52px; height: 52px; font-size: 1rem; }
    .photo { object-fit: cover; padding: 0; }
    .who { flex: 1; min-width: 140px; display: flex; flex-direction: column; }
    .actions { width: 100%; }
    .tabs button { display: inline-flex; }
  `,
})
export class ContactsPage {
  private readonly contacts = inject(ContactService);
  private readonly messaging = inject(MessagingService);
  private readonly router = inject(Router);

  protected readonly all = signal<Contact[]>([]);
  protected readonly tab = signal<Tab>('contacts');
  protected readonly error = signal<string | null>(null);

  protected readonly accepted = computed(() => this.all().filter((c) => c.status === 'ACCEPTED'));
  protected readonly incoming = computed(() => this.all().filter((c) => c.status === 'PENDING' && c.incoming));
  protected readonly outgoing = computed(() => this.all().filter((c) => c.status !== 'ACCEPTED' && !c.incoming));
  protected readonly shown = computed(() =>
    this.tab() === 'contacts' ? this.accepted() : this.tab() === 'incoming' ? this.incoming() : this.outgoing(),
  );

  constructor() {
    this.load(true);
  }

  protected initials(name: string): string {
    return name.split(' ').map((p) => p[0] ?? '').join('').slice(0, 2).toUpperCase();
  }

  protected message(c: Contact): void {
    this.messaging.openDirect(c.otherId).subscribe({
      next: (conv) => this.router.navigate(['/messages', conv.id]),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected accept(c: Contact): void {
    this.contacts.accept(c.id).subscribe({ next: () => this.load(), error: (e) => this.error.set(errorMessage(e)) });
  }

  protected decline(c: Contact): void {
    this.contacts.decline(c.id).subscribe({ next: () => this.load(), error: (e) => this.error.set(errorMessage(e)) });
  }

  protected remove(c: Contact): void {
    this.contacts.remove(c.id).subscribe({ next: () => this.load(), error: (e) => this.error.set(errorMessage(e)) });
  }

  /** Au premier chargement, ouvre l'onglet des demandes recues s'il y en a. */
  private load(first = false): void {
    this.contacts.list().subscribe({
      next: (list) => {
        this.all.set(list);
        if (first && this.incoming().length) this.tab.set('incoming');
        this.contacts.refreshPending();
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
