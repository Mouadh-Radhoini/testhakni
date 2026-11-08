import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Category, Page, ProviderPublic, Service, ServiceSearch } from '../models';

/** Catalogue public : categories, recherche, detail service, profil prestataire. SEARCH-01 a 04 */
@Injectable({ providedIn: 'root' })
export class CatalogService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  /** Les categories ne changent pas : un seul appel, partage. */
  private readonly categories$ = this.http
    .get<Category[]>(`${this.api}/categories`)
    .pipe(shareReplay({ bufferSize: 1, refCount: false }));

  categories(): Observable<Category[]> {
    return this.categories$;
  }

  search(criteria: ServiceSearch): Observable<Page<Service>> {
    let params = new HttpParams();
    for (const [key, value] of Object.entries(criteria)) {
      if (value !== null && value !== undefined && value !== '') {
        params = params.set(key, String(value));
      }
    }
    return this.http.get<Page<Service>>(`${this.api}/services`, { params });
  }

  service(id: string): Observable<Service> {
    return this.http.get<Service>(`${this.api}/services/${encodeURIComponent(id)}`);
  }

  provider(id: string): Observable<ProviderPublic> {
    return this.http.get<ProviderPublic>(`${this.api}/providers/${encodeURIComponent(id)}`);
  }
}
