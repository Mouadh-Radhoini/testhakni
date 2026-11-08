import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

export interface HealthStatus {
  status: string;
  application: string;
  timestamp: string;
}

/** Appelle GET /api/health du backend Spring Boot. */
@Injectable({ providedIn: 'root' })
export class HealthService {
  private readonly http = inject(HttpClient);

  check(): Observable<HealthStatus> {
    return this.http.get<HealthStatus>(`${environment.apiUrl}/health`);
  }
}
