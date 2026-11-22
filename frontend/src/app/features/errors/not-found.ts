import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '../../core/i18n/i18n';

/** Page 404. FE-01 */
@Component({
  selector: 'app-not-found',
  imports: [RouterLink, TranslatePipe],
  template: `
    <section class="container narrow empty">
      <p class="code">404</p>
      <h1>{{ 'notFound.title' | t }}</h1>
      <p>{{ 'notFound.text' | t }}</p>
      <a routerLink="/" class="btn">{{ 'notFound.home' | t }}</a>
    </section>
  `,
  styles: `.code { font-size: 5rem; font-weight: 800; letter-spacing: -0.05em; color: var(--c-primary-soft); margin: 0; line-height: 1; }
    .empty { border: 0; background: none; margin-top: 3rem; }`,
})
export class NotFound {}
