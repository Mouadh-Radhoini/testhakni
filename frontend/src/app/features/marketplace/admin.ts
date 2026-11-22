import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink, RouterLinkActive } from '@angular/router';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User, Service, SERVICE_STATUS_LABELS } from '../../core/models';
import { Review, Report } from '../../core/marketplace.models';
import { Feedback, PageState } from './page-state';

type Section = 'dashboard' | 'users' | 'services' | 'reviews' | 'reports';
interface Row {
  id: string;
  title: string;
  details: string;
  action?: string;
  enabled?: boolean;
}
const titles = {
  dashboard: 'Tableau de bord',
  users: 'Utilisateurs',
  services: 'Services',
  reviews: 'Avis',
  reports: 'Signalements',
};
const metrics: Record<string, string> = {
  users: 'Utilisateurs',
  services: 'Services',
  requests: 'Demandes',
  offers: 'Offres',
  bookings: 'Réservations',
  reviews: 'Avis',
  openReports: 'Signalements ouverts',
};

@Component({
  imports: [RouterLink, RouterLinkActive, Feedback, ReactiveFormsModule],
  template: `<section class="container">
    <nav class="tabs" aria-label="Administration">
      @for (key of sections; track key) {
        <a [routerLink]="['/admin', key]" routerLinkActive="active">{{ titles[key] }}</a>
      }
    </nav>
    <h1>{{ titles[section] }}</h1>
    <app-feedback [state]="state" />
    @if (section === 'users') {
      <div class="form-row mb">
        <label
          >Rôle<select [formControl]="role" (change)="load()">
            <option value="">Tous</option>
            <option value="CUSTOMER">Client</option>
            <option value="PROVIDER">Prestataire</option>
            <option value="ADMIN">Administrateur</option>
          </select></label
        >
        <label
          >État<select [formControl]="status" (change)="load()">
            <option value="">Tous</option>
            <option value="ACTIVE">Actif</option>
            <option value="SUSPENDED">Suspendu</option>
          </select></label
        >
      </div>
    }
    <div class="grid">
      @for (r of rows(); track r.id) {
        <article class="card">
          <h2>{{ r.title }}</h2>
          <p>{{ r.details }}</p>
          @if (r.action) {
            @if (selected() === r.id) {
              <label
                >Motif de la décision<textarea [formControl]="reason" maxlength="1000"></textarea>
              </label>
              <button class="btn" [disabled]="reason.invalid || state.loading()" (click)="save(r)">
                {{ r.action }}
              </button>
              @if (section === 'reports') {
                <button
                  class="btn secondary"
                  [disabled]="reason.invalid || state.loading()"
                  (click)="save(r, true)"
                >
                  Classer sans suite
                </button>
              }
            } @else {
              <button class="btn secondary" (click)="select(r)">{{ r.action }}</button>
            }
          }
        </article>
      } @empty {
        @if (!state.loading()) {
          <p>Aucun résultat.</p>
        }
      }
    </div>
  </section>`,
})
export class AdminPage implements OnInit {
  readonly titles = titles;
  readonly sections = Object.keys(titles) as Section[];
  readonly section: Section = inject(ActivatedRoute).snapshot.data['section'];
  private readonly http = inject(HttpClient);
  private readonly url = environment.apiUrl + '/admin/' + this.section;
  readonly state = new PageState();
  readonly rows = signal<Row[]>([]);
  readonly selected = signal('');
  readonly role = new FormControl('', { nonNullable: true });
  readonly status = new FormControl('', { nonNullable: true });
  readonly reason = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(1000)],
  });
  ngOnInit() {
    this.load();
  }
  select(row: Row) {
    this.reason.reset();
    this.selected.set(row.id);
  }
  load() {
    let query: Observable<Row[]>;
    switch (this.section) {
      case 'dashboard':
        query = this.http
          .get<Record<string, number>>(this.url)
          .pipe(
            map((data) =>
              Object.entries(data).map(([id, value]) => ({
                id,
                title: metrics[id],
                details: String(value),
              })),
            ),
          );
        break;
      case 'users': {
        const params: Record<string, string> = {};
        if (this.role.value) params['role'] = this.role.value;
        if (this.status.value) params['status'] = this.status.value;
        query = this.http.get<User[]>(this.url, { params }).pipe(
          map((list) =>
            list.map((u) => ({
              id: u.id,
              title: u.firstName + ' ' + u.lastName,
              details:
                u.email +
                ' · ' +
                { CUSTOMER: 'Client', PROVIDER: 'Prestataire', ADMIN: 'Administrateur' }[u.role] +
                ' · ' +
                (u.status === 'ACTIVE' ? 'Actif' : 'Suspendu'),
              enabled: u.status !== 'ACTIVE',
              action:
                u.role === 'ADMIN' ? undefined : u.status === 'ACTIVE' ? 'Suspendre' : 'Activer',
            })),
          ),
        );
        break;
      }
      case 'services':
        query = this.http.get<Service[]>(this.url).pipe(
          map((list) =>
            list.map((s) => ({
              id: s.id,
              title: s.title,
              details: s.description + ' · ' + SERVICE_STATUS_LABELS[s.status],
              enabled: s.status !== 'ACTIVE',
              action: s.status === 'ACTIVE' ? 'Suspendre' : 'Activer',
            })),
          ),
        );
        break;
      case 'reviews':
        query = this.http.get<Review[]>(this.url).pipe(
          map((list) =>
            list.map((r) => ({
              id: r.id,
              title: r.rating + '/5',
              details: r.comment,
              enabled: r.hidden,
              action: r.hidden ? 'Rendre visible' : 'Masquer',
            })),
          ),
        );
        break;
      case 'reports':
        query = this.http.get<Report[]>(this.url).pipe(
          map((list) =>
            list.map((r) => ({
              id: r.id,
              title: (r.targetType === 'SERVICE' ? 'Service' : 'Avis') + ' · ' + r.targetId,
              details: r.reason + ' · ' + (r.decision ?? 'Ouvert'),
              action: r.status === 'OPEN' ? 'Résoudre' : undefined,
            })),
          ),
        );
        break;
    }
    this.state.run(query, (rows) => this.rows.set(rows));
  }
  save(row: Row, dismiss = false) {
    const decision =
      this.section === 'reports'
        ? { status: dismiss ? 'DISMISSED' : 'RESOLVED' }
        : { enabled: row.enabled };
    this.state.run(
      this.http.patch(this.url + '/' + row.id, { ...decision, reason: this.reason.value }),
      () => {
        this.selected.set('');
        this.load();
      },
      'Décision enregistrée.',
    );
  }
}
