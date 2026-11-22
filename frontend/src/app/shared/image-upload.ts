import { tr } from '../core/i18n/i18n';

/** Regles d'envoi d'image partagees, alignees sur le backend (JPEG, PNG ou WebP, 2 Mo). */
export const IMAGE_ACCEPT = 'image/jpeg,image/png,image/webp';
export const IMAGE_MAX_BYTES = 2 * 1024 * 1024;

/** Verification cote navigateur pour un retour immediat ; le serveur revérifie le contenu reel. */
export function imageProblem(file: File): string | null {
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
    return tr('image.badFormat');
  }
  if (file.size > IMAGE_MAX_BYTES) {
    return tr('image.tooBig');
  }
  return null;
}

/** Fichier choisi dans un <input type="file">, puis remise a zero pour pouvoir rechoisir le meme. */
export function takeFile(event: Event): File | null {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0] ?? null;
  input.value = '';
  return file;
}
