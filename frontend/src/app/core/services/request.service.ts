import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { RequestStatus, ServiceCategory, ServiceRequest } from '../models';

export interface RequestPayload {
  title: string;
  description: string;
  category: ServiceCategory;
  city: string;
  address?: string | null;
  preferredDate?: string | null;
  budget?: number | null;
  serviceId?: string | null;
}

/** Demandes du client connecte. REQ-01 / REQ-02 */
@Injectable({ providedIn: 'root' })
export class RequestService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/requests`;

  create(body: RequestPayload): Observable<ServiceRequest> {
    return this.http.post<ServiceRequest>(this.api, body);
  }

  mine(status?: RequestStatus | ''): Observable<ServiceRequest[]> {
    const params = status ? new HttpParams().set('status', status) : undefined;
    return this.http.get<ServiceRequest[]>(`${this.api}/mine`, { params });
  }

  cancel(id: string): Observable<ServiceRequest> {
    return this.http.patch<ServiceRequest>(`${this.api}/${id}/cancel`, {});
  }
}
