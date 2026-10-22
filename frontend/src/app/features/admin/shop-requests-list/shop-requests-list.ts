import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { ShopService } from '../../../core/shop/shop.service';
import { ShopAdminDetailResponse } from '../../../core/shop/shop.model';

const STATUS_FILTERS = [
  { value: 'EN_ATTENTE', label: 'En attente' },
  { value: 'VALIDEE', label: 'Validées (vendeurs actifs)' },
  { value: 'REJETEE', label: 'Rejetées' },
  { value: 'SUSPENDUE', label: 'Suspendues' },
];

@Component({
  selector: 'app-shop-requests-list',
  imports: [RouterLink, DatePipe, FormsModule],
  templateUrl: './shop-requests-list.html',
  styleUrl: './shop-requests-list.scss',
})
export class ShopRequestsList {
  private readonly shopService = inject(ShopService);

  readonly statusFilters = STATUS_FILTERS;
  readonly selectedStatus = signal<string>('EN_ATTENTE');
  readonly requests = signal<ShopAdminDetailResponse[]>([]);
  readonly loading = signal(true);
  readonly searchQuery = signal('');

  readonly filteredRequests = computed(() => {
    const q = this.searchQuery().trim().toLowerCase();
    if (!q) return this.requests();
    return this.requests().filter(
      (r) =>
        r.shop.name.toLowerCase().includes(q) ||
        r.ownerUsername.toLowerCase().includes(q) ||
        (r.shop.addressCity ?? '').toLowerCase().includes(q),
    );
  });

  constructor() {
    this.load();
  }

  selectStatus(status: string): void {
    this.selectedStatus.set(status);
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.shopService.listForAdmin(this.selectedStatus()).subscribe({
      next: (page) => {
        this.requests.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
