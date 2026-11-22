import { tr } from './i18n';

/**
 * Table de libelles reactive : chaque lecture (labels[status]) traduit dans la langue courante.
 * Remplace les anciennes constantes figees en francais, sans changer le code des pages.
 */
export function labelTable<K extends string>(prefix: string, keys: readonly K[]): Record<K, string> {
  const table = {} as Record<K, string>;
  for (const key of keys) {
    Object.defineProperty(table, key, { enumerable: true, get: () => tr(`${prefix}.${key}`) });
  }
  return table;
}
