import { PricePipe } from './price.pipe';

describe('PricePipe', () => {
  const pipe = new PricePipe();

  it('formats fixed, hourly and on-quote prices', () => {
    expect(pipe.transform(60, 'FIXED')).toBe('60 DT');
    expect(pipe.transform(35, 'HOURLY')).toBe('35 DT / h');
    expect(pipe.transform(null, 'ON_QUOTE')).toBe('Sur devis');
    expect(pipe.transform(null, 'FIXED')).toBe('Sur devis');
  });
});
