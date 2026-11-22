import { ReviewsPanel } from '../marketplace/reviews';
import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';

import { errorMessage } from '../../core/api-error';
import { ProviderPublic } from '../../core/models';
import { CatalogService } from '../../core/services/catalog.service';
import { ServiceCard } from '../../shared/service-card';
import { Icon } from '../../shared/icon';
import { TranslatePipe } from '../../core/i18n/i18n';
import { LocalDatePipe } from '../../shared/local-date.pipe';
import { ContactButton } from '../../shared/contact-button';
import { AuthService } from '../../core/services/auth.service';

/** Profil public d'un prestataire et ses services (PROV-03 / SEARCH-04). FE-06 */
@Component({
  selector: 'app-provider-public',
  imports: [ReviewsPanel, RouterLink, DatePipe, DecimalPipe, ServiceCard, Icon, TranslatePipe, LocalDatePipe, ContactButton],
  template: `
    <section class="container">
      @if (error()) {
        <div class="card empty">
          <p>{{ error() }}</p>
          <a class="btn" routerLink="/services">{{ 'detail.seeServices' | t }}</a>
        </div>
      } @else if (provider(); as p) {
        <div class="card mb profile">
          <div class="row">
            @if (p.avatarUrl) {
              <img class="avatar xl photo" [src]="p.avatarUrl" [alt]="'provider.photoOf' | t: { name: p.displayName }" />
            } @else {
              <span class="avatar xl">{{ p.businessName.slice(0, 2).toUpperCase() }}</span>
            }
            <h1>{{ p.businessName }}</h1>
            <span class="badge" [class.ACTIVE]="p.available">{{ (p.available ? 'detail.available' : 'detail.unavailable') | t }}</span>
          </div>
          <p class="muted">
            {{ p.displayName }} · <app-icon name="pin" [size]="15" class="inline" /> {{ p.city }}
            @if (p.serviceArea) { ({{ p.serviceArea }}) }
            @if (p.yearsOfExperience) { · {{ 'detail.years' | t: { n: p.yearsOfExperience } }} }
            · {{ 'provider.memberSince' | t: { date: (p.memberSince | localDate: 'MMMM yyyy') } }}
          </p>
          @if (p.bio) { <p class="bio">{{ p.bio }}</p> }
          <div class="row">
            @for (c of p.categories; track c) { <span class="badge">{{ ('cat.' + c) | t }}</span> }
          </div>
          <p class="stats">
            <span class="badge accent"><app-icon name="check" [size]="14" /> {{ 'provider.jobs' | t: { n: p.completedJobs ?? 0 } }}</span>
            @if (p.reviewCount) {
              <span class="badge"><app-icon name="star" [size]="14" /> {{ p.averageRating | number: '1.1-1' }} · {{ 'provider.reviewsCount' | t: { n: p.reviewCount } }}</span>
            }
          </p>
          @if (auth.role() === 'CUSTOMER') {
            <div class="contact"><app-contact-button [userId]="p.providerId" /></div>
          }
          @if (p.skills.length) {
            <p class="small mt"><strong>{{ 'provider.skills' | t }}</strong> {{ p.skills.join(', ') }}</p>
          }
        </div>

        @if (p.photoUrls.length) {
          <h2>{{ 'provider.works' | t }}</h2>
          <div class="gallery">
            @for (url of p.photoUrls; track url) {
              <a [href]="url" target="_blank" rel="noopener"><img [src]="url" [alt]="'portfolio.alt' | t" loading="lazy" /></a>
            }
          </div>
        }

        <h2>{{ 'provider.services' | t: { n: p.services.length } }}</h2>
        @if (p.services.length === 0) {
          <div class="card empty">{{ 'provider.noServices' | t }}</div>
        } @else {
          <div class="grid">
            @for (s of p.services; track s.id) { <app-service-card [service]="s" /> }
          </div>
        }
        <app-reviews group="providers" [id]="p.providerId"/>
      } @else {
        <p class="muted">{{ 'common.loading' | t }}</p>
      }
    </section>
  `,
  styles: `
    .bio { white-space: pre-line; }
    .stats { display: flex; flex-wrap: wrap; gap: 0.5rem; margin: 0.75rem 0 0; }
    .stats .badge { display: inline-flex; align-items: center; gap: 0.3rem; }
    .contact { margin-top: 1rem; padding-top: 1rem; border-top: 1px solid var(--c-border); }
    .profile h1 { margin: 0; flex: 1; }
    .xl { width: 64px; height: 64px; font-size: 1.3rem; border-radius: 18px; background: linear-gradient(145deg, var(--c-primary), #1f9a80); }
    .inline { vertical-align: -2px; display: inline-flex; }
    h2 { margin-top: 2rem; }
    .photo { object-fit: cover; padding: 0; }
    .gallery { display: grid; gap: 0.75rem; grid-template-columns: repeat(auto-fill, minmax(min(150px, 45%), 1fr)); }
    .gallery a { display: block; aspect-ratio: 4 / 3; border-radius: var(--radius); overflow: hidden; background: var(--c-surface-2); }
    .gallery img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.2s; }
    .gallery a:hover img { transform: scale(1.04); }
  `,
})
export class ProviderPublicPage implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly catalog = inject(CatalogService);

  readonly id = input.required<string>();

  protected readonly provider = signal<ProviderPublic | null>(null);
  protected readonly error = signal<string | null>(null);
  private readonly categories = toSignal(this.catalog.categories(), { initialValue: [] });
  private readonly labels = computed(() => new Map(this.categories().map((c) => [c.code as string, c.label])));

  ngOnInit(): void {
    this.catalog.provider(this.id()).subscribe({
      next: (p) => this.provider.set(p),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected label(code: string): string {
    return this.labels().get(code) ?? code;
  }
}
