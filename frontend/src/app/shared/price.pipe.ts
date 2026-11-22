import { Pipe, PipeTransform } from '@angular/core';

import { tr } from '../core/i18n/i18n';
import { PriceType } from '../core/models';

/** Affiche "60 DT", "35 DT / h" ou "Sur devis", dans la langue courante. */
@Pipe({ name: 'price', pure: false })
export class PricePipe implements PipeTransform {
  transform(price: number | null | undefined, type: PriceType): string {
    if (type === 'ON_QUOTE' || price === null || price === undefined) {
      return tr('price.onQuote');
    }
    const amount = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 2 }).format(price);
    const value = `${amount} ${tr('price.currency')}`;
    return type === 'HOURLY' ? `${value} ${tr('price.perHour')}` : value;
  }
}
