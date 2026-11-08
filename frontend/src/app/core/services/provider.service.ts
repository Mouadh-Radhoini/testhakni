import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ProviderProfile, Service, ServicePayload, ServiceStatus } from '../models';

export type ProviderProfilePayload = Omit<ProviderProfile, 'id' | 'userId'>;

/** Espace prestataire : profil professionnel et gestion de ses services. PROV / SERV */
@Injectable({ providedIn: 'root' })
export class ProviderService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  myProfile(): Observable<ProviderProfile> {
    return this.http.get<ProviderProfile>(`${this.api}/providers/me/profile`);
  }

  saveProfile(body: ProviderProfilePayload): Observable<ProviderProfile> {
    return this.http.put<ProviderProfile>(`${this.api}/providers/me/profile`, body);
  }

  myServices(): Observable<Service[]> {
    return this.http.get<Service[]>(`${this.api}/services/mine`);
  }

  createService(body: ServicePayload): Observable<Service> {
    return this.http.post<Service>(`${this.api}/services`, body);
  }

  updateService(id: string, body: ServicePayload): Observable<Service> {
    return this.http.put<Service>(`${this.api}/services/${id}`, body);
  }

  setStatus(id: string, status: ServiceStatus): Observable<Service> {
    return this.http.patch<Service>(`${this.api}/services/${id}/status`, { status });
  }

  deleteService(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/services/${id}`);
  }
}
