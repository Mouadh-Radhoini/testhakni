import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Icon } from '../../shared/icon';

/** Choix du type de compte avant l'inscription. FE-02 */
@Component({
  selector: 'app-register-choice',
  imports: [RouterLink, Icon],
  template: `
    <section class="container choose">
      <div class="page-head center">
        <div>
          <h1>Créer un compte</h1>
          <p>Gratuit, en moins de deux minutes. Choisissez votre profil :</p>
        </div>
      </div>
      <div class="choices">
        <a class="card choice" routerLink="/register/customer">
          <span class="icon-tile big"><app-icon name="home" [size]="28" /></span>
          <h2>Je suis client</h2>
          <p class="muted">Je cherche un prestataire et je publie mes demandes.</p>
          <span class="btn secondary">Créer un compte client</span>
        </a>
        <a class="card choice" routerLink="/register/provider">
          <span class="icon-tile accent big"><app-icon name="briefcase" [size]="28" /></span>
          <h2>Je suis prestataire</h2>
          <p class="muted">Je propose mes services et je reçois des demandes.</p>
          <span class="btn">Devenir prestataire</span>
        </a>
      </div>
      <p class="muted mt center">Déjà inscrit ? <a routerLink="/login">Connexion</a></p>
    </section>
  `,
  styles: `
    .choose { max-width: 860px; }
    .center { justify-content: center; text-align: center; }
    .choices { display: grid; gap: 1.25rem; grid-template-columns: 1fr 1fr; }
    @media (max-width: 640px) { .choices { grid-template-columns: 1fr; } }
    .choice { color: inherit; font-weight: inherit; text-align: center; display: flex; flex-direction: column;
      align-items: center; gap: 0.6rem; padding: 2.2rem 1.6rem; transition: transform 0.15s, box-shadow 0.15s, border-color 0.15s; }
    .choice:hover { border-color: var(--c-primary); text-decoration: none; transform: translateY(-3px); box-shadow: var(--shadow); color: inherit; }
    .choice h2 { margin: 0.4rem 0 0; }
    .choice p { margin: 0 0 0.6rem; }
    .big { width: 64px; height: 64px; border-radius: 18px; }
  `,
})
export class RegisterChoice {}
