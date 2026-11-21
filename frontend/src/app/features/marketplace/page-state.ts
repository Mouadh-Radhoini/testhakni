import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DestroyRef, input } from '@angular/core';
import { finalize, Observable } from 'rxjs';
import { errorMessage } from '../../core/api-error';

export class PageState {
  readonly loading = signal(false);
  readonly error = signal('');
  readonly message = signal('');
  private readonly destroy = inject(DestroyRef);

  private pending = 0;

  run<T>(request: Observable<T>, done: (result: T) => void, success = '', failed?: () => void) {
    this.pending++;
    this.loading.set(true);
    this.error.set('');
    request
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.loading.set(--this.pending > 0)),
      )
      .subscribe({
        next: (result) => {
          if (success) this.message.set(success);
          done(result);
        },
        error: (err) => {
          this.error.set(errorMessage(err));
          failed?.();
        },
      });
  }
}

@Component({
  selector: 'app-feedback',
  template: `
    @if (state().loading()) {
      <p role="status">Chargement…</p>
    }
    @if (state().error()) {
      <p class="alert error" role="alert">{{ state().error() }}</p>
    }
    @if (state().message()) {
      <p class="alert success" role="status">{{ state().message() }}</p>
    }
  `,
})
export class Feedback {
  readonly state = input.required<PageState>();
}
