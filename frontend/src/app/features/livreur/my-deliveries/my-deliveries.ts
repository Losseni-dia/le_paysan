import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { LivreurService } from '../../../core/livreur/livreur.service';
import { AdminOrderResponse } from '../../../core/order/order.model';

@Component({
  selector: 'app-my-deliveries',
  imports: [RouterLink, FormsModule],
  templateUrl: './my-deliveries.html',
  styleUrl: './my-deliveries.scss',
})
export class MyDeliveries {
  private readonly livreurService = inject(LivreurService);

  readonly orders = signal<AdminOrderResponse[]>([]);
  readonly loading = signal(true);
  readonly updatingId = signal<number | null>(null);
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

  markPickedUp(order: AdminOrderResponse): void {
    this.updatingId.set(order.id);
    this.livreurService.markPickedUp(order.id).subscribe({
      next: (updated) => {
        this.orders.set(this.orders().map((o) => (o.id === updated.id ? updated : o)));
        this.updatingId.set(null);
      },
      error: () => this.updatingId.set(null),
    });
  }

  markEnRoute(order: AdminOrderResponse): void {
    this.updatingId.set(order.id);
    this.livreurService.markEnRoute(order.id).subscribe({
      next: (updated) => {
        this.orders.set(this.orders().map((o) => (o.id === updated.id ? updated : o)));
        this.updatingId.set(null);
      },
      error: () => this.updatingId.set(null),
    });
  }

  private load(): void {
    this.livreurService.listMyDeliveries().subscribe({
      next: (page) => {
        this.orders.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
