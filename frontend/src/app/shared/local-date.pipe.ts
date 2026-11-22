import { formatDate } from '@angular/common';
import { inject, Pipe, PipeTransform } from '@angular/core';

import { I18n } from '../core/i18n/i18n';

/** Date formatee dans la langue de l'interface (noms de mois traduits). */
@Pipe({ name: 'localDate', pure: false })
export class LocalDatePipe implements PipeTransform {
  private readonly i18n = inject(I18n);

  transform(value: string | Date | null | undefined, format = 'd MMMM yyyy'): string {
    if (!value) return '';
    return formatDate(value, format, this.i18n.locale().slice(0, 2) === 'en' ? 'en-US' : this.i18n.locale().slice(0, 2));
  }
}
