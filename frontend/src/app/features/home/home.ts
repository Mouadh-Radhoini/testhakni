import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { HealthService } from '../../core/services/health.service';

type ApiState = 'loading' | 'up' | 'down';

@Component({
  selector: 'app-home',
  imports: [FormsModule, RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home implements OnInit {
  private readonly healthService = inject(HealthService);
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);

  protected readonly apiState = signal<ApiState>('loading');
  protected query = '';

  ngOnInit(): void {
    this.healthService.check().subscribe({
      next: (res) => this.apiState.set(res.status === 'UP' ? 'up' : 'down'),
      error: () => this.apiState.set('down'),
    });
  }

  search(): void {
    const q = this.query.trim();
    this.router.navigate(['/services'], { queryParams: q ? { q } : {} });
  }
}
