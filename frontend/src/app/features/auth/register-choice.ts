import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Choix du type de compte avant l'inscription. FE-02 */
@Component({
  selector: 'app-register-choice',
  imports: [RouterLink],
  template: `
    <section class="container">
      <h1>Créer un compte</h1>
      <div class="grid">
        <a class="card choice" routerLink="/register/customer">
          <span class="icon">🙋</span>
          <h2>Je suis client</h2>
          <p class="muted">Je cherche un prestataire et je publie mes demandes.</p>
        </a>
        <a class="card choice" routerLink="/register/provider">
          <span class="icon">🧰</span>
          <h2>Je suis prestataire</h2>
          <p class="muted">Je propose mes services et je reçois des demandes.</p>
        </a>
      </div>
      <p class="muted mt">Déjà inscrit ? <a routerLink="/login">Connexion</a></p>
    </section>
  `,
  styles: `
    .choice { color: inherit; text-align: center; transition: border-color 0.15s; }
    .choice:hover { border-color: var(--c-primary); text-decoration: none; }
    .icon { font-size: 2.2rem; }
  `,
})
export class RegisterChoice {}
