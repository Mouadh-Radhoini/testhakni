import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { MarketplaceService } from '../../core/services/marketplace.service';
import { OpenRequests } from './requests';
import { BookingsPage } from './bookings';

describe('Marketplace pages', () => {
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('loads eligible requests and validates submissions', () => {
    const fixture = TestBed.createComponent(OpenRequests);
    fixture.detectChanges();
    http
      .expectOne('/api/providers/me/requests')
      .flush([{ id: 'r', title: 'Fuite', category: 'PLOMBERIE', city: 'Tunis' }]);
    const page = fixture.componentInstance;
    page.select('r');
    page.submit('r');
    http.expectNone('/api/requests/r/offers');
    page.form.setValue({ price: 25, proposedDate: '2030-04-15', description: 'Réparation' });
    page.submit('r');
    const submission = http.expectOne('/api/requests/r/offers');
    expect(submission.request.method).toBe('POST');
    expect(submission.request.body.price).toBe(25);
    submission.flush({});
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Offre envoyée.');
  });

  it('shows an ownership error on a private booking', () => {
    const fixture = TestBed.createComponent(BookingsPage);
    fixture.componentRef.setInput('id', 'foreign');
    fixture.detectChanges();
    http
      .expectOne('/api/bookings/foreign')
      .flush({ message: 'Réservation privée' }, { status: 403, statusText: 'Forbidden' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role=alert]').textContent).toContain(
      'Réservation privée',
    );
  });

  it('marks the chosen notification as read', () => {
    TestBed.inject(MarketplaceService).read('n1').subscribe();
    const request = http.expectOne('/api/notifications/n1/read');
    expect(request.request.method).toBe('PATCH');
    request.flush(null);
  });
});
