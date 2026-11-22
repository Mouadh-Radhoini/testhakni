import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink, RouterLinkActive } from '@angular/router';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { forkJoin, map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User, Service, SERVICE_STATUS_LABELS } from '../../core/models';
import { Review, Report } from '../../core/marketplace.models';
import { Feedback, PageState } from './page-state';

type Section = 'dashboard' | 'users' | 'services' | 'reviews' | 'reports';

/** Une ligne de la liste d'administration, deja prete a afficher. */
interface Row {
  id: string;
  title: string;
  details: string;
  action?: string;
  enabled?: boolean;
  /** Petite etiquette au-dessus du titre (type de contenu, role...). */
  kicker?: string;
  /** Pastille d'etat a droite du titre. */
  badge?: string;
  badgeClass?: string;
  /** Ligne d'information secondaire (date, ville, decision...). */
  meta?: string;
  /** Initiales affichees dans un rond (utilisateurs). */
  avatar?: string;
  /** Carte de statistique du tableau de bord. */
  stat?: boolean;
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
const ROLES = { CUSTOMER: 'Client', PROVIDER: 'Prestataire', ADMIN: 'Administrateur' } as const;
const REPORT_STATUS = {
  OPEN: { text: 'Ouvert', cls: 'OPEN' },
  RESOLVED: { text: 'Résolu', cls: 'RESOLVED' },
  DISMISSED: { text: 'Classé sans suite', cls: 'DISMISSED' },
} as const;

const date = (iso: string) => new Date(iso).toLocaleDateString('fr-FR');
const excerpt = (text: string, max = 70) => (text.length > max ? text.slice(0, max - 1) + '…' : text);

@Component({
  imports: [RouterLink, RouterLinkActive, Feedback, ReactiveFormsModule],
  styles: `
    .head { display: flex; align-items: flex-start; gap: 0.75rem; }
    .titles { flex: 1; min-width: 0; display: flex; flex-direction: column; align-items: flex-start; gap: 0.35rem; }
    .titles h2 { margin: 0; font-size: 1.08rem; overflow-wrap: anywhere; }
    .details { margin: 0.7rem 0 0; overflow-wrap: anywhere; }
    .meta { margin: 0.35rem 0 0; }
    .actions { margin-top: 1rem; }
    .list { display: grid; gap: 1rem; grid-template-columns: repeat(auto-fill, minmax(min(320px, 100%), 1fr)); align-items: start; }
    .list .card { margin: 0; }
    .stat-card { display: flex; flex-direction: column; gap: 0.2rem; }
    .stat-card h2 { margin: 0; font-size: 0.9rem; color: var(--c-muted); font-weight: 650; letter-spacing: 0; }
    .stat-card .number { font-size: 2.2rem; font-weight: 800; letter-spacing: -0.03em; line-height: 1.1; }
    .filters { display: flex; flex-wrap: wrap; gap: 1rem; margin-bottom: 1.25rem; }
    .filters label { min-width: 160px; margin: 0; }
    .decision { display: grid; gap: 0.6rem; margin-top: 0.9rem; }
    .decision textarea { min-height: 90px; }
  `,
  template: `<section class="container">
    <nav class="tabs" aria-label="Administration">
      @for (key of sections; track key) {
        <a [routerLink]="['/admin', key]" routerLinkActive="active">{{ titles[key] }}</a>
      }
    </nav>
    <h1>{{ titles[section] }}</h1>
    <app-feedback [state]="state" />
    @if (section === 'users') {
      <div class="filters">
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
    <div class="list">
      @for (r of rows(); track r.id) {
        @if (r.stat) {
          <article class="card stat-card">
            <h2>{{ r.title }}</h2>
            <span class="number">{{ r.details }}</span>
          </article>
        } @else {
          <article class="card">
            <div class="head">
              @if (r.avatar) { <span class="avatar" aria-hidden="true">{{ r.avatar }}</span> }
              <div class="titles">
                @if (r.kicker) { <span class="badge accent">{{ r.kicker }}</span> }
                <h2>{{ r.title }}</h2>
              </div>
              @if (r.badge) { <span class="badge" [class]="r.badgeClass">{{ r.badge }}</span> }
            </div>
            <p class="details">{{ r.details }}</p>
            @if (r.meta) { <p class="meta muted small">{{ r.meta }}</p> }
            @if (r.action) {
              <div class="actions">
                @if (selected() === r.id) {
                  <div class="decision">
                    <label
                      >Motif de la décision<textarea [formControl]="reason" maxlength="1000"></textarea>
                    </label>
                    <div class="row">
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
                      <button class="btn ghost" type="button" (click)="selected.set('')">Annuler</button>
                    </div>
                  </div>
                } @else {
                  <button class="btn secondary" (click)="select(r)">{{ r.action }}</button>
                }
              </div>
            }
          </article>
        }
      } @empty {
        @if (!state.loading()) {
          <div class="card empty">Aucun résultat.</div>
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
  private readonly api = environment.apiUrl + '/admin/';
  private readonly url = this.api + this.section;
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
                stat: true,
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
              details: u.email,
              kicker: ROLES[u.role],
              avatar: `${u.firstName?.[0] ?? ''}${u.lastName?.[0] ?? ''}`.toUpperCase(),
              badge: u.status === 'ACTIVE' ? 'Actif' : 'Suspendu',
              badgeClass: u.status,
              meta: [u.city, u.createdAt ? 'inscrit le ' + date(u.createdAt) : ''].filter(Boolean).join(' · '),
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
              details: s.description,
              kicker: s.categoryLabel,
              badge: SERVICE_STATUS_LABELS[s.status],
              badgeClass: s.status,
              meta: s.city,
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
              title: '★'.repeat(r.rating) + '☆'.repeat(5 - r.rating) + '  ' + r.rating + '/5',
              details: r.comment,
              kicker: 'Avis client',
              badge: r.hidden ? 'Masqué' : 'Visible',
              badgeClass: r.hidden ? 'SUSPENDED' : 'ACTIVE',
              meta: date(r.createdAt),
              enabled: r.hidden,
              action: r.hidden ? 'Rendre visible' : 'Masquer',
            })),
          ),
        );
        break;
      case 'reports':
        // Les signalements ne portent qu'un identifiant : on charge aussi services et avis
        // pour afficher le nom du service ou le texte de l'avis signale.
        query = forkJoin({
          reports: this.http.get<Report[]>(this.url),
          services: this.http.get<Service[]>(this.api + 'services'),
          reviews: this.http.get<Review[]>(this.api + 'reviews'),
        }).pipe(
          map(({ reports, services, reviews }) =>
            reports.map((r) => {
              const isService = r.targetType === 'SERVICE';
              const service = isService ? services.find((s) => s.id === r.targetId) : undefined;
              const review = isService ? undefined : reviews.find((x) => x.id === r.targetId);
              const found = service ?? review;
              const status = REPORT_STATUS[r.status];
              return {
                id: r.id,
                kicker: isService ? 'Service signalé' : 'Avis signalé',
                title: service
                  ? service.title
                  : review
                    ? `« ${excerpt(review.comment)} » (${review.rating}/5)`
                    : `Contenu introuvable (…${r.targetId.slice(-6)})`,
                details: r.reason,
                badge: status.text,
                badgeClass: status.cls,
                meta:
                  'Signalé le ' + date(r.createdAt) + (r.decision ? ' · Décision : ' + r.decision : '') +
                  (found ? '' : ' · contenu supprimé'),
                action: r.status === 'OPEN' ? 'Résoudre' : undefined,
              } satisfies Row;
            }),
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
