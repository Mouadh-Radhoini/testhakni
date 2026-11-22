import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
import {
  Booking,
  BookingStatus,
  Notice,
  Offer,
  OpenRequest,
  Proposal,
  Report,
  Review,
  Reviews,
} from '../marketplace.models';
import { Page } from '../models';

@Injectable({ providedIn: 'root' })
export class MarketplaceService {
  readonly http = inject(HttpClient);
  readonly api = environment.apiUrl;
  readonly noticeRevision = signal(0);
  open() {
    return this.http.get<OpenRequest[]>(this.api + '/providers/me/requests');
  }
  submit(id: string, body: Proposal) {
    return this.http.post<Offer>(this.api + '/requests/' + id + '/offers', body);
  }
  offers(id?: string) {
    return this.http.get<Offer[]>(
      this.api + (id ? '/requests/' + id + '/offers' : '/providers/me/offers'),
    );
  }
  accept(o: Offer) {
    return this.http.post<Booking>(
      this.api + '/requests/' + o.requestId + '/offers/' + o.id + '/accept',
      {},
    );
  }
  withdraw(id: string) {
    return this.http.patch<void>(this.api + '/providers/me/offers/' + id + '/withdraw', {});
  }
  bookings() {
    return this.http.get<Booking[]>(this.api + '/bookings');
  }
  booking(id: string) {
    return this.http.get<Booking>(this.api + '/bookings/' + id);
  }
  progress(id: string, status: BookingStatus) {
    return this.http.patch<Booking>(this.api + '/bookings/' + id + '/status', { status });
  }
  /** Le client declare la prestation en cours, terminee ou l'annule. */
  customerStatus(id: string, status: 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED') {
    return this.http.patch<Booking>(this.api + '/bookings/' + id + '/customer-status', { status });
  }
  confirm(id: string) {
    return this.http.post<Booking>(this.api + '/bookings/' + id + '/confirm', {});
  }
  review(id: string, rating: number, comment: string) {
    return this.http.post<Review>(this.api + '/bookings/' + id + '/review', { rating, comment });
  }
  reviews(group: 'services' | 'providers', id: string) {
    return this.http.get<Reviews>(this.api + '/' + group + '/' + id + '/reviews');
  }
  report(targetType: 'SERVICE' | 'REVIEW', targetId: string, reason: string) {
    return this.http.post<Report>(this.api + '/reports', { targetType, targetId, reason });
  }
  notices(page: number) {
    return this.http.get<Page<Notice>>(this.api + '/notifications', { params: { page } });
  }
  unread() {
    return this.http.get<number>(this.api + '/notifications/unread');
  }
  read(id: string) {
    return this.http.patch<void>(this.api + '/notifications/' + id + '/read', {});
  }
}
