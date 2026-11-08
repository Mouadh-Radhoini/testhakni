import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, FormGroup } from '@angular/forms';

import { ApiError } from './models';

/** Message lisible a partir d'une erreur HTTP (format ApiError du backend). */
export function errorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return "Impossible de joindre l'API. Le backend est-il lancé ?";
    }
    const body = err.error as Partial<ApiError> | null;
    if (body?.message) {
      return body.message;
    }
  }
  return 'Une erreur inattendue est survenue.';
}

/**
 * Recopie les erreurs champ par champ du backend sur les controles du formulaire
 * (erreur "server"), pour les afficher sous chaque champ.
 */
export function applyServerErrors(form: FormGroup, err: unknown): void {
  if (!(err instanceof HttpErrorResponse)) return;
  const fields = (err.error as Partial<ApiError> | null)?.fieldErrors ?? {};
  for (const [name, message] of Object.entries(fields)) {
    const control: AbstractControl | null = form.get(name);
    if (control) {
      control.setErrors({ ...(control.errors ?? {}), server: message });
      control.markAsTouched();
    }
  }
}

/** Premier message d'erreur d'un controle, en francais. */
export function controlError(control: AbstractControl | null): string | null {
  if (!control || !control.errors || !(control.touched || control.dirty)) return null;
  const e = control.errors;
  if (e['server']) return e['server'];
  if (e['required']) return 'Champ obligatoire';
  if (e['email']) return 'Email invalide';
  if (e['minlength']) return `${e['minlength'].requiredLength} caractères minimum`;
  if (e['maxlength']) return `${e['maxlength'].requiredLength} caractères maximum`;
  if (e['min']) return `Valeur minimale : ${e['min'].min}`;
  if (e['max']) return `Valeur maximale : ${e['max'].max}`;
  if (e['pattern']) return 'Format invalide';
  if (e['password']) return e['password'];
  return 'Valeur invalide';
}

/** Meme politique que le backend (AUTH-07). */
export const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,72}$/;
export const PASSWORD_HINT =
  '8 caractères minimum, avec une majuscule, une minuscule, un chiffre et un caractère spécial.';
export const PHONE_PATTERN = /^[+0-9 ()-]{8,20}$/;
