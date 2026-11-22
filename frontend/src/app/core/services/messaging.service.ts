import { HttpClient, HttpParams } from '@angular/common/http';
import { DestroyRef, effect, inject, Injectable, signal } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { catchError, EMPTY, Observable, Subject, tap, timer } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

export interface Conversation {
  id: string;
  requestId: string | null;
  title: string | null;
  otherParticipantId: string;
  otherParticipantName: string;
  otherParticipantAvatarUrl: string | null;
  lastMessagePreview: string | null;
  lastMessageAt: string | null;
  unread: number;
}

export interface ChatMessage {
  id: string;
  conversationId: string;
  senderId: string;
  body: string;
  sentAt: string;
}

/**
 * Messagerie client / prestataire.
 * - REST pour l'historique, l'envoi et les compteurs ;
 * - STOMP sur WebSocket (/ws) pour recevoir les nouveaux messages en temps reel,
 *   authentifie par le JWT d'acces envoye dans la trame CONNECT.
 */
@Injectable({ providedIn: 'root' })
export class MessagingService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly api = `${environment.apiUrl}/conversations`;

  /** Nouveaux messages recus en direct (envoyes par moi depuis un autre onglet compris). */
  readonly incoming = new Subject<ChatMessage>();
  readonly unread = signal(0);
  readonly connected = signal(false);

  private client: Client | null = null;

  constructor() {
    // Connexion temps reel ouverte tant qu'un client ou un prestataire est connecte
    effect((cleanup) => {
      const user = this.auth.user();
      this.unread.set(0);
      if (!user || (user.role !== 'CUSTOMER' && user.role !== 'PROVIDER')) return;
      this.connect();
      // Filet de securite si le WebSocket est coupe (proxy, veille) : recompte toutes les minutes
      const poll = timer(0, 60_000).subscribe(() => this.refreshUnread());
      cleanup(() => {
        poll.unsubscribe();
        this.disconnect();
      });
    });
    inject(DestroyRef).onDestroy(() => this.disconnect());
  }

  list(): Observable<Conversation[]> {
    return this.http.get<Conversation[]>(this.api);
  }

  get(id: string): Observable<Conversation> {
    return this.http.get<Conversation>(`${this.api}/${encodeURIComponent(id)}`);
  }

  openForOffer(offerId: string): Observable<Conversation> {
    return this.http.post<Conversation>(`${this.api}/by-offer/${encodeURIComponent(offerId)}`, null);
  }

  /** Discussion directe avec un contact accepte. */
  openDirect(userId: string): Observable<Conversation> {
    return this.http.post<Conversation>(`${this.api}/with/${encodeURIComponent(userId)}`, null);
  }

  openForBooking(bookingId: string): Observable<Conversation> {
    return this.http.post<Conversation>(`${this.api}/by-booking/${encodeURIComponent(bookingId)}`, null);
  }

  messages(id: string, before?: string): Observable<ChatMessage[]> {
    const params = before ? new HttpParams().set('before', before) : undefined;
    return this.http.get<ChatMessage[]>(`${this.api}/${encodeURIComponent(id)}/messages`, { params });
  }

  send(id: string, body: string): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(`${this.api}/${encodeURIComponent(id)}/messages`, { body });
  }

  markRead(id: string): Observable<void> {
    return this.http.post<void>(`${this.api}/${encodeURIComponent(id)}/read`, null).pipe(tap(() => this.refreshUnread()));
  }

  refreshUnread(): void {
    this.http
      .get<{ unread: number }>(`${this.api}/unread`)
      .pipe(catchError(() => EMPTY))
      .subscribe((r) => this.unread.set(r.unread));
  }

  private connect(): void {
    if (this.client || typeof window === 'undefined' || typeof WebSocket === 'undefined') return;
    const scheme = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const client = new Client({
      brokerURL: `${scheme}://${window.location.host}/ws`,
      reconnectDelay: 5000,
      heartbeatIncoming: 20000,
      heartbeatOutgoing: 20000,
      // Relu a chaque (re)connexion : le jeton d'acces a pu etre renouvele entre-temps
      beforeConnect: (c) => {
        c.connectHeaders = { Authorization: `Bearer ${this.auth.accessToken ?? ''}` };
      },
      onConnect: () => {
        this.connected.set(true);
        client.subscribe('/user/queue/messages', (frame) => {
          const message = JSON.parse(frame.body) as ChatMessage;
          this.incoming.next(message);
          if (message.senderId !== this.auth.user()?.id) this.refreshUnread();
        });
      },
      onWebSocketClose: () => this.connected.set(false),
      onStompError: () => this.connected.set(false),
    });
    this.client = client;
    client.activate();
  }

  private disconnect(): void {
    const client = this.client;
    this.client = null;
    this.connected.set(false);
    void client?.deactivate();
  }
}
