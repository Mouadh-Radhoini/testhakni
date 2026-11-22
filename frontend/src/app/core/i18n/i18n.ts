import { Injectable, Pipe, PipeTransform, signal } from '@angular/core';

import { AR } from './ar';
import { EN } from './en';
import { FR } from './fr';

export type Lang = 'fr' | 'en' | 'ar';
export type Dictionary = Record<keyof typeof FR, string>;

export const LANGS: { code: Lang; label: string; short: string }[] = [
  { code: 'fr', label: 'Français', short: 'FR' },
  { code: 'en', label: 'English', short: 'EN' },
  { code: 'ar', label: 'العربية', short: 'ع' },
];

const DICTIONARIES: Record<Lang, Dictionary> = { fr: FR, en: EN, ar: AR };
const STORAGE_KEY = 'testehkni.lang';

function initialLang(): Lang {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved === 'fr' || saved === 'en' || saved === 'ar') return saved;
  } catch {
    /* stockage indisponible */
  }
  // Le francais est la langue par defaut du site ; le choix du visiteur est ensuite memorise
  return 'fr';
}

/** Langue courante, partagee par le service, le pipe et les fonctions utilitaires. */
const current = signal<Lang>(initialLang());

/**
 * Traduit une cle ; {nom} est remplace par params.nom.
 * Repli : francais, puis la cle elle-meme (visible en developpement si une traduction manque).
 */
export function tr(key: string, params?: Record<string, string | number>): string {
  const dict = DICTIONARIES[current()] as Record<string, string>;
  let text = dict[key] ?? (FR as Record<string, string>)[key] ?? key;
  if (params) {
    for (const [name, value] of Object.entries(params)) text = text.replaceAll(`{${name}}`, String(value));
  }
  return text;
}

/** Langue de l'interface (FR / EN / AR), sens de lecture et choix memorise. */
@Injectable({ providedIn: 'root' })
export class I18n {
  readonly lang = current.asReadonly();
  readonly langs = LANGS;

  constructor() {
    this.apply(current());
  }

  isRtl(): boolean {
    return current() === 'ar';
  }

  /** Locale pour les dates et nombres (pipes Angular, Intl). */
  locale(): string {
    return { fr: 'fr-FR', en: 'en-GB', ar: 'ar-TN' }[current()];
  }

  use(lang: Lang): void {
    current.set(lang);
    try {
      localStorage.setItem(STORAGE_KEY, lang);
    } catch {
      /* stockage indisponible */
    }
    this.apply(lang);
  }

  private apply(lang: Lang): void {
    if (typeof document === 'undefined') return;
    document.documentElement.lang = lang;
    document.documentElement.dir = lang === 'ar' ? 'rtl' : 'ltr';
  }
}

/** {{ 'nav.find' | t }} ou {{ 'search.count' | t: { n: total } }} ; se met a jour au changement de langue. */
@Pipe({ name: 't', pure: false })
export class TranslatePipe implements PipeTransform {
  transform(key: string, params?: Record<string, string | number>): string {
    return tr(key, params);
  }
}
