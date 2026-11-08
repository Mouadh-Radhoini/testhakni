import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Page 404. FE-01 */
@Component({
  selector: 'app-not-found',
  imports: [RouterLink],
  template: `
    <section class="container narrow empty">
      <h1>404 — Page introuvable</h1>
      <p>La page demandée n'existe pas ou a été déplacée.</p>
      <a routerLink="/" class="btn">Retour à l'accueil</a>
    </section>
  `,
})
export class NotFound {}
