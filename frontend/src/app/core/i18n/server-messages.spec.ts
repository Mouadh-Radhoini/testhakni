import { serverText } from './server-messages';

describe('serverText', () => {
  it('keeps French untouched', () => {
    expect(serverText('Service introuvable', 'fr')).toBe('Service introuvable');
  });

  it('translates known server messages into English and Arabic', () => {
    expect(serverText('Service introuvable', 'en')).toBe('Service not found');
    expect(serverText('Service introuvable', 'ar')).toBe('الخدمة غير موجودة');
  });

  it('translates messages that end with a variable part', () => {
    expect(serverText('Nouvelle offre pour Fuite cuisine', 'en')).toBe('New offer for Fuite cuisine');
    expect(serverText('Trop de tentatives. Réessayez dans 42 secondes.', 'en')).toBe('Too many attempts. Try again in 42 seconds.');
  });

  it('never loses an unknown message', () => {
    expect(serverText('Message inconnu', 'ar')).toBe('Message inconnu');
    expect(serverText(null, 'en')).toBe('');
  });
});
