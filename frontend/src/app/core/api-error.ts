import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, FormGroup } from '@angular/forms';

import { tr } from './i18n/i18n';
import { serverMessage } from './i18n/server-messages';
import { ApiError } from './models';

/** Message lisible a partir d'une erreur HTTP (format ApiError du backend). */
export function errorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return tr('err.network');
    }
    if (err.status === 503) {
      return tr('err.unavailable');
    }
    const body = err.error as Partial<ApiError> | null;
    if (body?.message) {
      return serverMessage(body.message);
    }
  }
  return tr('err.unexpected');
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

/** Premier message d'erreur d'un controle, dans la langue de l'interface. */
export function controlError(control: AbstractControl | null): string | null {
  if (!control || !control.errors || !(control.touched || control.dirty)) return null;
  const e = control.errors;
  if (e['server']) return serverMessage(e['server']);
  if (e['required']) return tr('val.required');
  if (e['email']) return tr('val.email');
  if (e['minlength']) return tr('val.minlength', { n: e['minlength'].requiredLength });
  if (e['maxlength']) return tr('val.maxlength', { n: e['maxlength'].requiredLength });
  if (e['min']) return tr('val.min', { n: e['min'].min });
  if (e['max']) return tr('val.max', { n: e['max'].max });
  if (e['pattern']) return tr('val.pattern');
  if (e['password']) return e['password'];
  return tr('val.invalid');
}

/** Meme politique que le backend (AUTH-07). */
export const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,72}$/;
export const PASSWORD_HINT =
  '8 caractères minimum, avec une majuscule, une minuscule, un chiffre et un caractère spécial.';
export const PHONE_PATTERN = /^[+0-9 ()-]{8,20}$/;
