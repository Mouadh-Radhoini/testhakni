import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Affichee quand un guard refuse l'acces a cause du role. FE-03 */
@Component({
  selector: 'app-forbidden',
  imports: [RouterLink],
  template: `
    <section class="container narrow empty">
      <h1>Accès refusé</h1>
      <p>Cette page n'est pas disponible pour votre type de compte.</p>
      <a routerLink="/" class="btn">Retour à l'accueil</a>
    </section>
  `,
})
export class Forbidden {}
