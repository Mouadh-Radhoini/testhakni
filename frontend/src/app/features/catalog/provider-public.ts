import { ReviewsPanel } from '../marketplace/reviews';
import { DatePipe } from '@angular/common';
import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';

import { errorMessage } from '../../core/api-error';
import { ProviderPublic } from '../../core/models';
import { CatalogService } from '../../core/services/catalog.service';
import { ServiceCard } from '../../shared/service-card';
import { Icon } from '../../shared/icon';

/** Profil public d'un prestataire et ses services (PROV-03 / SEARCH-04). FE-06 */
@Component({
  selector: 'app-provider-public',
  imports: [ReviewsPanel, RouterLink, DatePipe, ServiceCard, Icon],
  template: `
    <section class="container">
      @if (error()) {
        <div class="card empty">
          <p>{{ error() }}</p>
          <a class="btn" routerLink="/services">Voir les services</a>
        </div>
      } @else if (provider(); as p) {
        <div class="card mb profile">
          <div class="row">
            @if (p.avatarUrl) {
              <img class="avatar xl photo" [src]="p.avatarUrl" [alt]="'Photo de ' + p.displayName" />
            } @else {
              <span class="avatar xl">{{ p.businessName.slice(0, 2).toUpperCase() }}</span>
            }
            <h1>{{ p.businessName }}</h1>
            <span class="badge" [class.ACTIVE]="p.available">{{ p.available ? 'Disponible' : 'Indisponible' }}</span>
          </div>
          <p class="muted">
            {{ p.displayName }} · <app-icon name="pin" [size]="15" class="inline" /> {{ p.city }}
            @if (p.serviceArea) { ({{ p.serviceArea }}) }
            @if (p.yearsOfExperience) { · {{ p.yearsOfExperience }} ans d'expérience }
            · membre depuis {{ p.memberSince | date: 'MMMM yyyy' }}
          </p>
          @if (p.bio) { <p class="bio">{{ p.bio }}</p> }
          <div class="row">
            @for (c of p.categories; track c) { <span class="badge">{{ label(c) }}</span> }
          </div>
          @if (p.skills.length) {
            <p class="small mt"><strong>Compétences :</strong> {{ p.skills.join(', ') }}</p>
          }
        </div>

        @if (p.photoUrls.length) {
          <h2>Réalisations</h2>
          <div class="gallery">
            @for (url of p.photoUrls; track url) {
              <a [href]="url" target="_blank" rel="noopener"><img [src]="url" alt="Réalisation de {{ p.businessName }}" loading="lazy" /></a>
            }
          </div>
        }

        <h2>Services proposés ({{ p.services.length }})</h2>
        @if (p.services.length === 0) {
          <div class="card empty">Ce prestataire n'a pas encore de service actif.</div>
        } @else {
          <div class="grid">
            @for (s of p.services; track s.id) { <app-service-card [service]="s" /> }
          </div>
        }
        <app-reviews group="providers" [id]="p.providerId"/>
      } @else {
        <p class="muted">Chargement…</p>
      }
    </section>
  `,
  styles: `
    .bio { white-space: pre-line; }
    .profile h1 { margin: 0; flex: 1; }
    .xl { width: 64px; height: 64px; font-size: 1.3rem; border-radius: 18px; background: linear-gradient(145deg, var(--c-primary), #1f9a80); }
    .inline { vertical-align: -2px; display: inline-flex; }
    h2 { margin-top: 2rem; }
    .photo { object-fit: cover; padding: 0; }
    .gallery { display: grid; gap: 0.75rem; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); }
    .gallery a { display: block; aspect-ratio: 4 / 3; border-radius: var(--radius); overflow: hidden; background: var(--c-surface-2); }
    .gallery img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.2s; }
    .gallery a:hover img { transform: scale(1.04); }
  `,
})
export class ProviderPublicPage implements OnInit {
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
