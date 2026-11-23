import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CheckoutResponse {
  paymentId: string;
  bookingId: string;
  checkoutUrl: string;
  checkoutSessionId: string;
  status: 'PENDING' | 'PAID' | 'FAILED' | 'CANCELLED' | 'EXPIRED';
}

export interface PaymentStatusResponse {
  paymentId: string;
  bookingId: string;
  amount: number;
  currency: string;
  status: 'PENDING' | 'PAID' | 'FAILED' | 'CANCELLED' | 'EXPIRED';
  paidAt: string | null;
}

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/payments`;

  checkout(bookingId: string): Observable<CheckoutResponse> {
    return this.http.post<CheckoutResponse>(`${this.api}/bookings/${bookingId}/checkout`, {});
  }

  status(bookingId: string): Observable<PaymentStatusResponse> {
    return this.http.get<PaymentStatusResponse>(`${this.api}/bookings/${bookingId}`);
  }
}
