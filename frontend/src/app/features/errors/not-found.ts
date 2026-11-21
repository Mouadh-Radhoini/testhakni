import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Page 404. FE-01 */
@Component({
  selector: 'app-not-found',
  imports: [RouterLink],
  template: `
    <section class="container narrow empty">
      <p class="code">404</p>
      <h1>Page introuvable</h1>
      <p>La page demandée n'existe pas ou a été déplacée.</p>
      <a routerLink="/" class="btn">Retour à l'accueil</a>
    </section>
  `,
  styles: `.code { font-size: 5rem; font-weight: 800; letter-spacing: -0.05em; color: var(--c-primary-soft); margin: 0; line-height: 1; }
    .empty { border: 0; background: none; margin-top: 3rem; }`,
})
export class NotFound {}
