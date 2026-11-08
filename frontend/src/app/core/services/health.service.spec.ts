import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { HealthService } from './health.service';
import { environment } from '../../../environments/environment';

describe('HealthService', () => {
  let service: HealthService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(HealthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('calls GET /health on the API', () => {
    service.check().subscribe((res) => expect(res.status).toBe('UP'));
    const req = http.expectOne(`${environment.apiUrl}/health`);
    expect(req.request.method).toBe('GET');
    req.flush({ status: 'UP', application: 'testhakni-backend', timestamp: '' });
  });
});
