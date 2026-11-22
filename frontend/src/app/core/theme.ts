import { Injectable, signal } from '@angular/core';

export type Theme = 'light' | 'dark';
const STORAGE_KEY = 'testehkni.theme';

/**
 * Mode clair / sombre. Par defaut : preference du systeme ; un choix explicite est memorise.
 * Le theme est porte par l'attribut data-theme de <html>, lu par les variables CSS.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly theme = signal<Theme>(this.initial());

  constructor() {
    this.apply(this.theme());
  }

  toggle(): void {
    const next: Theme = this.theme() === 'dark' ? 'light' : 'dark';
    this.theme.set(next);
    try {
      localStorage.setItem(STORAGE_KEY, next);
    } catch {
      /* stockage indisponible */
    }
    this.apply(next);
  }

  private initial(): Theme {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved === 'light' || saved === 'dark') return saved;
    } catch {
      /* stockage indisponible */
    }
    return typeof matchMedia !== 'undefined' && matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }

  private apply(theme: Theme): void {
    if (typeof document === 'undefined') return;
    document.documentElement.dataset['theme'] = theme;
    document.querySelector('meta[name="theme-color"]')?.setAttribute('content', theme === 'dark' ? '#0d1714' : '#0f5e4f');
  }
}
