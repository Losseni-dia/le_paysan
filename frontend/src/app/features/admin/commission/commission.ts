import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { CommissionService } from '../../../core/commission/commission.service';
import { CommissionRateResponse } from '../../../core/commission/commission.model';

@Component({
  selector: 'app-commission',
  imports: [DatePipe, FormsModule],
  templateUrl: './commission.html',
  styleUrl: './commission.scss',
})
export class Commission {
  private readonly commissionService = inject(CommissionService);

  readonly current = signal<CommissionRateResponse | null>(null);
  readonly history = signal<CommissionRateResponse[]>([]);
  readonly loading = signal(true);
  readonly newRate = signal<number | null>(null);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);

  constructor() {
    this.load();
  }

  save(): void {
    const rate = this.newRate();
    if (rate == null || rate < 0 || rate > 100) {
      this.error.set('Entrez un taux valide entre 0 et 100.');
      return;
    }
    this.saving.set(true);
    this.error.set(null);
    this.success.set(null);
    this.commissionService.update(rate).subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set('Taux de commission mis à jour.');
        this.newRate.set(null);
        this.load();
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.error.set(err.error?.message ?? 'Impossible de mettre à jour le taux.');
      },
    });
  }

  private load(): void {
    this.loading.set(true);
    this.commissionService.getCurrent().subscribe({
      next: (current) => this.current.set(current),
    });
    this.commissionService.getHistory().subscribe({
      next: (history) => {
        this.history.set(history);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
