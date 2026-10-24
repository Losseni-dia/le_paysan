import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { OrderService } from '../../../core/order/order.service';
import { AdminOrderResponse } from '../../../core/order/order.model';

const STATUS_FILTERS = [
  { value: '', label: 'Toutes' },
  { value: 'PAYEE', label: 'Payées' },
  { value: 'EN_PREPARATION', label: 'En préparation' },
  { value: 'PRETE', label: 'Prêtes' },
  { value: 'EXPEDIEE', label: 'Expédiées' },
  { value: 'LIVREE', label: 'Livrées' },
  { value: 'ANNULEE', label: 'Annulées' },
];

@Component({
  selector: 'app-vendor-orders-list',
  imports: [RouterLink, DatePipe, FormsModule],
  templateUrl: './orders-list.html',
  styleUrl: './orders-list.scss',
})
export class OrdersList {
  private readonly orderService = inject(OrderService);

  readonly statusFilters = STATUS_FILTERS;
  readonly selectedStatus = signal('PAYEE');
  readonly orders = signal<AdminOrderResponse[]>([]);
  readonly loading = signal(true);
  readonly searchQuery = signal('');

  readonly filteredOrders = computed(() => {
    const q = this.searchQuery().trim().toLowerCase();
    if (!q) return this.orders();
    return this.orders().filter(
      (o) =>
        o.orderNumber.toLowerCase().includes(q) ||
        o.buyerFullName.toLowerCase().includes(q) ||
        o.items.some((i) => i.productName.toLowerCase().includes(q)),
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
    this.orderService.listForVendor(this.selectedStatus() || undefined).subscribe({
      next: (page) => {
        this.orders.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
