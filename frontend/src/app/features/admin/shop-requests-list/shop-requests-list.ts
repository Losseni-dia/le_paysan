import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { ShopService } from '../../../core/shop/shop.service';
import { ShopAdminDetailResponse } from '../../../core/shop/shop.model';

const STATUS_FILTERS = ['EN_ATTENTE', 'VALIDEE', 'REJETEE', 'SUSPENDUE'];

@Component({
  selector: 'app-shop-requests-list',
  imports: [RouterLink, DatePipe],
  templateUrl: './shop-requests-list.html',
  styleUrl: './shop-requests-list.scss',
})
export class ShopRequestsList {
  private readonly shopService = inject(ShopService);

  readonly statusFilters = STATUS_FILTERS;
  readonly selectedStatus = signal<string>('EN_ATTENTE');
  readonly requests = signal<ShopAdminDetailResponse[]>([]);
  readonly loading = signal(true);

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
