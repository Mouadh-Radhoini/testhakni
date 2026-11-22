import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '../../core/i18n/i18n';

/** Affichee quand un guard refuse l'acces a cause du role. FE-03 */
@Component({
  selector: 'app-forbidden',
  imports: [RouterLink, TranslatePipe],
  template: `
    <section class="container narrow empty">
      <h1>{{ 'fb.title' | t }}</h1>
      <p>{{ 'fb.text' | t }}</p>
      <a routerLink="/" class="btn">{{ 'notFound.home' | t }}</a>
    </section>
  `,
})
export class Forbidden {}
