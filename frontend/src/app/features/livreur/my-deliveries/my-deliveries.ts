import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { LivreurService } from '../../../core/livreur/livreur.service';
import { AdminOrderResponse } from '../../../core/order/order.model';

@Component({
  selector: 'app-my-deliveries',
  imports: [RouterLink],
  templateUrl: './my-deliveries.html',
  styleUrl: './my-deliveries.scss',
})
export class MyDeliveries {
  private readonly livreurService = inject(LivreurService);

  readonly orders = signal<AdminOrderResponse[]>([]);
  readonly loading = signal(true);
  readonly updatingId = signal<number | null>(null);

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
