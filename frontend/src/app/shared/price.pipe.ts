import { Pipe, PipeTransform } from '@angular/core';

import { PriceType } from '../core/models';

/** Affiche "60 DT", "35 DT / h" ou "Sur devis". */
@Pipe({ name: 'price' })
export class PricePipe implements PipeTransform {
  transform(price: number | null | undefined, type: PriceType): string {
    if (type === 'ON_QUOTE' || price === null || price === undefined) {
      return 'Sur devis';
    }
    const amount = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 2 }).format(price);
    return type === 'HOURLY' ? `${amount} DT / h` : `${amount} DT`;
  }
}
