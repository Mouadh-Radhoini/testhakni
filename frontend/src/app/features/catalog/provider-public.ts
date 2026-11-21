import { ReviewsPanel } from '../marketplace/reviews';
import { DatePipe } from '@angular/common';
import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';

import { errorMessage } from '../../core/api-error';
import { ProviderPublic } from '../../core/models';
import { CatalogService } from '../../core/services/catalog.service';
import { ServiceCard } from '../../shared/service-card';

/** Profil public d'un prestataire et ses services (PROV-03 / SEARCH-04). FE-06 */
@Component({
  selector: 'app-provider-public',
  imports: [ReviewsPanel, RouterLink, DatePipe, ServiceCard],
  template: `
    <section class="container">
      @if (error()) {
        <div class="card empty">
          <p>{{ error() }}</p>
          <a class="btn" routerLink="/services">Voir les services</a>
        </div>
      } @else if (provider(); as p) {
        <div class="card mb">
          <div class="row">
            <h1>{{ p.businessName }}</h1>
            <span class="badge" [class.ACTIVE]="p.available">{{ p.available ? 'Disponible' : 'Indisponible' }}</span>
          </div>
          <p class="muted">
            {{ p.displayName }} · 📍 {{ p.city }}
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
  styles: `.bio { white-space: pre-line; }`,
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
