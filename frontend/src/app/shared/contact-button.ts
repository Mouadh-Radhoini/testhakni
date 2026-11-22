import { Component, effect, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';

import { errorMessage } from '../core/api-error';
import { TranslatePipe } from '../core/i18n/i18n';
import { ContactService, ContactState } from '../core/services/contact.service';
import { MessagingService } from '../core/services/messaging.service';
import { Icon } from './icon';

/**
 * Bouton de contact affiche sur un profil : ajouter, demande envoyee (annuler),
 * demande recue (accepter / refuser), contact etabli (envoyer un message).
 */
@Component({
  selector: 'app-contact-button',
  imports: [Icon, TranslatePipe],
  template: `
    @switch (state().state) {
      @case ('ACCEPTED') {
        <div class="row">
          <span class="badge ACCEPTED"><app-icon name="userCheck" [size]="14" /> {{ 'contact.connected' | t }}</span>
          <button type="button" class="btn small" [disabled]="busy()" (click)="message()">
            <app-icon name="chat" [size]="16" /> {{ 'contact.message' | t }}
          </button>
        </div>
      }
      @case ('PENDING_OUT') {
        <div class="row">
          <span class="badge SUBMITTED">{{ 'contact.pendingOut' | t }}</span>
          <button type="button" class="btn ghost small" [disabled]="busy()" (click)="cancel()">{{ 'contact.cancel' | t }}</button>
        </div>
      }
      @case ('PENDING_IN') {
        <div class="row">
          <span class="small muted">{{ 'contact.pendingIn' | t }}</span>
          <button type="button" class="btn small" [disabled]="busy()" (click)="accept()">{{ 'contact.accept' | t }}</button>
          <button type="button" class="btn ghost small" [disabled]="busy()" (click)="decline()">{{ 'contact.decline' | t }}</button>
        </div>
      }
      @default {
        <div class="row">
          @if (state().state === 'DECLINED') { <span class="badge DISMISSED">{{ 'contact.declined' | t }}</span> }
          <button type="button" class="btn secondary small" [disabled]="busy()" (click)="add()">
            <app-icon name="userPlus" [size]="16" /> {{ (state().state === 'DECLINED' ? 'contact.retry' : 'contact.add') | t }}
          </button>
        </div>
      }
    }
    @if (state().state === 'NONE' || state().state === 'PENDING_OUT') {
      <p class="small muted why">{{ 'contact.why' | t }}</p>
    }
    @if (error()) { <p class="small error" role="alert">{{ error() }}</p> }
  `,
  styles: `
    .why { margin: 0.4rem 0 0; }
    .error { color: var(--c-danger); margin: 0.4rem 0 0; }
    .badge { display: inline-flex; align-items: center; gap: 0.3rem; }
  `,
})
export class ContactButton {
  private readonly contacts = inject(ContactService);
  private readonly messaging = inject(MessagingService);
  private readonly router = inject(Router);

  /** Compte de l'autre personne. */
  readonly userId = input.required<string>();

  protected readonly state = signal<ContactState>({ contactId: null, state: 'NONE' });
  protected readonly busy = signal(false);
  protected readonly error = signal<string | null>(null);

  constructor() {
    effect(() => {
      const id = this.userId();
      this.contacts.stateWith(id).subscribe({ next: (s) => this.state.set(s), error: () => undefined });
    });
  }

  protected add(): void {
    this.run(this.contacts.request(this.userId()), (c) =>
      this.state.set({ contactId: c.id, state: c.status === 'ACCEPTED' ? 'ACCEPTED' : 'PENDING_OUT' }),
    );
  }

  protected cancel(): void {
    const id = this.state().contactId;
    if (id) this.run(this.contacts.remove(id), () => this.state.set({ contactId: null, state: 'NONE' }));
  }

  protected accept(): void {
    const id = this.state().contactId;
    if (id) this.run(this.contacts.accept(id), () => this.state.set({ contactId: id, state: 'ACCEPTED' }));
  }

  protected decline(): void {
    const id = this.state().contactId;
    if (id) this.run(this.contacts.decline(id), () => this.state.set({ contactId: null, state: 'NONE' }));
  }

  protected message(): void {
    this.run(this.messaging.openDirect(this.userId()), (c) => this.router.navigate(['/messages', c.id]));
  }

  private run<T>(request: import('rxjs').Observable<T>, done: (value: T) => void): void {
    this.busy.set(true);
    this.error.set(null);
    request.subscribe({
      next: (value) => {
        this.busy.set(false);
        done(value);
      },
      error: (e) => {
        this.busy.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }
}
