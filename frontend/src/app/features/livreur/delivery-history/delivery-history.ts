import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { LivreurService } from '../../../core/livreur/livreur.service';
import { AdminOrderResponse } from '../../../core/order/order.model';

@Component({
  selector: 'app-delivery-history',
  imports: [DatePipe, FormsModule],
  templateUrl: './delivery-history.html',
  styleUrl: './delivery-history.scss',
})
export class DeliveryHistory {
  private readonly livreurService = inject(LivreurService);

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
    this.livreurService.listMyDeliveryHistory().subscribe({
      next: (page) => {
        this.orders.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  deliveredAt(order: AdminOrderResponse): string {
    const delivered = [...order.historique].reverse().find((h) => h.status === 'LIVREE');
    return delivered?.changedAt ?? order.createdAt;
  }
}
