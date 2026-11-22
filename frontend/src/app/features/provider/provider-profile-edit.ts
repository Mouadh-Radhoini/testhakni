import { Component, inject, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { applyServerErrors, controlError, errorMessage } from '../../core/api-error';
import { ServiceCategory } from '../../core/models';
import { AuthService } from '../../core/services/auth.service';
import { CatalogService } from '../../core/services/catalog.service';
import { ProviderService } from '../../core/services/provider.service';
import { PortfolioManager } from '../../shared/portfolio-manager';
import { TranslatePipe } from '../../core/i18n/i18n';

/** Creation / modification du profil professionnel (PROV-01 / PROV-02). FE-04 */
@Component({
  selector: 'app-provider-profile-edit',
  imports: [ReactiveFormsModule, RouterLink, PortfolioManager, TranslatePipe],
  template: `
    <section class="container narrow">
      <div class="row mb">
        <h1>Mon profil professionnel</h1>
        <span class="spacer"></span>
        <a class="btn secondary small" [routerLink]="['/providers', auth.user()?.id]">Voir mon profil public</a>
      </div>
      <div class="card">
        <form class="form" [formGroup]="form" (ngSubmit)="save()" novalidate>
          @if (msg(); as m) { <div class="alert" [class]="m.type">{{ m.text }}</div> }
          <div class="field">
            <label for="businessName">Nom commercial</label>
            <input id="businessName" formControlName="businessName" />
            @if (err('businessName'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <div class="field">
            <label for="bio">Présentation</label>
            <textarea id="bio" formControlName="bio" placeholder="Votre expérience, vos points forts…"></textarea>
            @if (err('bio'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <fieldset class="field">
            <label>Catégories</label>
            <div class="checks">
              @for (c of categories(); track c.code) {
                <label>
                  <input type="checkbox" [checked]="isSelected(c.code)" (change)="toggle(c.code)" />
                  {{ c.label }}
                </label>
              }
            </div>
            @if (err('categories'); as e) { <span class="error">{{ e }}</span> }
          </fieldset>
          <div class="field">
            <label for="skills">Compétences</label>
            <input id="skills" formControlName="skills" placeholder="Fuites, Chauffe-eau, Tableau électrique" />
            <span class="hint">Séparées par des virgules (20 maximum).</span>
            @if (err('skills'); as e) { <span class="error">{{ e }}</span> }
          </div>
          <div class="form-row">
            <div class="field">
              <label for="city">Ville</label>
              <input id="city" formControlName="city" />
              @if (err('city'); as e) { <span class="error">{{ e }}</span> }
            </div>
            <div class="field">
              <label for="serviceArea">Zone d'intervention</label>
              <input id="serviceArea" formControlName="serviceArea" placeholder="Grand Tunis, 20 km…" />
            </div>
          </div>
          <div class="form-row">
            <div class="field">
              <label for="years">Années d'expérience</label>
              <input id="years" type="number" min="0" max="60" formControlName="yearsOfExperience" />
              @if (err('yearsOfExperience'); as e) { <span class="error">{{ e }}</span> }
            </div>
            <div class="field checks">
              <label><input type="checkbox" formControlName="available" /> Disponible pour de nouvelles demandes</label>
            </div>
          </div>
          <div><button class="btn" type="submit" [disabled]="saving()">Enregistrer le profil</button></div>
        </form>
      </div>

      <div class="card mt">
        <h2>{{ 'portfolio.title' | t }}</h2>
        @if (hasProfile()) {
          <p class="muted small">{{ 'portfolio.hint' | t }}</p>
          <app-portfolio-manager [(photos)]="photos" />
        } @else {
          <p class="muted small">{{ 'portfolio.needProfile' | t }}</p>
        }
      </div>
    </section>
  `,
  styles: `fieldset { border: 0; padding: 0; margin: 0; } .checks { align-self: end; }`,
})
export class ProviderProfileEdit implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly providers = inject(ProviderService);
  protected readonly auth = inject(AuthService);
  protected readonly categories = toSignal(inject(CatalogService).categories(), { initialValue: [] });

  protected readonly saving = signal(false);
  protected readonly photos = signal<string[]>([]);
  protected readonly hasProfile = signal(false);
  protected readonly msg = signal<{ type: string; text: string } | null>(null);

  protected readonly form = this.fb.group({
    businessName: ['', [Validators.required, Validators.maxLength(100)]],
    bio: ['', Validators.maxLength(1000)],
    categories: this.fb.control<ServiceCategory[]>([], Validators.required),
    skills: [''],
    city: ['', [Validators.required, Validators.maxLength(80)]],
    serviceArea: ['', Validators.maxLength(120)],
    yearsOfExperience: this.fb.control<number | null>(null, [Validators.min(0), Validators.max(60)]),
    available: [true],
  });

  ngOnInit(): void {
    this.providers.myProfile().subscribe({
      next: (p) => {
        this.hasProfile.set(true);
        this.photos.set(p.photoUrls ?? []);
        this.form.setValue({
          businessName: p.businessName,
          bio: p.bio ?? '',
          categories: p.categories,
          skills: p.skills.join(', '),
          city: p.city ?? '',
          serviceArea: p.serviceArea ?? '',
          yearsOfExperience: p.yearsOfExperience,
          available: p.available,
        });
      },
      // 404 = profil pas encore cree : formulaire vide
      error: () => this.msg.set({ type: 'info', text: 'Complétez votre profil pour être visible des clients.' }),
    });
  }

  protected err(name: string) {
    return controlError(this.form.get(name));
  }

  protected isSelected(code: ServiceCategory): boolean {
    return this.form.controls.categories.value.includes(code);
  }

  protected toggle(code: ServiceCategory): void {
    const ctrl = this.form.controls.categories;
    ctrl.setValue(ctrl.value.includes(code) ? ctrl.value.filter((c) => c !== code) : [...ctrl.value, code]);
    ctrl.markAsTouched();
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const v = this.form.getRawValue();
    const skills = v.skills
      .split(',')
      .map((s) => s.trim())
      .filter((s) => s.length > 0);
    this.providers
      .saveProfile({ ...v, skills, bio: v.bio || null, serviceArea: v.serviceArea || null })
      .subscribe({
        next: (p) => {
          this.saving.set(false);
          this.hasProfile.set(true);
          this.photos.set(p.photoUrls ?? []);
          this.msg.set({ type: 'success', text: 'Profil enregistré.' });
        },
        error: (e) => {
          this.saving.set(false);
          this.msg.set({ type: 'error', text: errorMessage(e) });
          applyServerErrors(this.form, e);
        },
      });
  }
}
