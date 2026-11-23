import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CheckoutResponse, PaymentService } from './payment.service';

describe('PaymentService', () => {
  let service: PaymentService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PaymentService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(PaymentService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('creates checkout without sending amount or currency', () => {
    const response: CheckoutResponse = {
      paymentId: 'payment-1',
      bookingId: 'booking-1',
      checkoutUrl: 'https://checkout.stripe.test/session',
      checkoutSessionId: 'cs_test',
      status: 'PENDING',
    };

    service.checkout('booking-1').subscribe((value) => expect(value).toEqual(response));

    const request = http.expectOne('/api/payments/bookings/booking-1/checkout');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({});
    request.flush(response);
  });

  it('loads payment status', () => {
    service.status('booking-1').subscribe();
    const request = http.expectOne('/api/payments/bookings/booking-1');
    expect(request.request.method).toBe('GET');
    request.flush({
      paymentId: 'payment-1',
      bookingId: 'booking-1',
      amount: 125,
      currency: 'tnd',
      status: 'PAID',
      paidAt: '2026-11-22T00:00:00Z',
    });
  });
});
