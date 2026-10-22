import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { PayoutService } from '../../../core/payout/payout.service';
import { VendorPayoutResponse } from '../../../core/payout/payout.model';

const STATUS_FILTERS = [
  { value: 'EN_ATTENTE', label: 'En attente' },
  { value: 'ENVOYE', label: 'Envoyés' },
  { value: 'ECHEC', label: 'Échecs' },
];

@Component({
  selector: 'app-payouts',
  imports: [DatePipe, FormsModule],
  templateUrl: './payouts.html',
  styleUrl: './payouts.scss',
})
export class Payouts {
  private readonly payoutService = inject(PayoutService);

  readonly statusFilters = STATUS_FILTERS;
  readonly selectedStatus = signal<string>('EN_ATTENTE');
  readonly payouts = signal<VendorPayoutResponse[]>([]);
  readonly loading = signal(true);
  readonly retryingId = signal<number | null>(null);
  readonly searchQuery = signal('');

  readonly filteredPayouts = computed(() => {
    const q = this.searchQuery().trim().toLowerCase();
    if (!q) return this.payouts();
    return this.payouts().filter(
      (p) => p.orderNumber.toLowerCase().includes(q) || p.shopName.toLowerCase().includes(q),
    );
  });

  constructor() {
    this.load();
  }

  selectStatus(status: string): void {
    this.selectedStatus.set(status);
    this.load();
  }

  retry(payout: VendorPayoutResponse): void {
    this.retryingId.set(payout.id);
    this.payoutService.retry(payout.id).subscribe({
      next: () => {
        this.retryingId.set(null);
        this.load();
      },
      error: () => this.retryingId.set(null),
    });
  }

  private load(): void {
    this.loading.set(true);
    this.payoutService.listForAdmin(this.selectedStatus()).subscribe({
      next: (page) => {
        this.payouts.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
