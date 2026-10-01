import { Component, inject, signal } from '@angular/core';

import { OrderService } from '../../../core/order/order.service';
import { OrderResponse } from '../../../core/order/order.model';

const STATUS_LABELS: Record<string, string> = {
  EN_ATTENTE_PAIEMENT: 'En attente de paiement',
  PAYEE: 'Payée',
  EN_PREPARATION: 'En préparation',
  PRETE: 'Prête (retrait)',
  EXPEDIEE: 'Expédiée',
  LIVREE: 'Livrée',
  ANNULEE: 'Annulée',
};

@Component({
  selector: 'app-my-orders',
  imports: [],
  templateUrl: './my-orders.html',
  styleUrl: './my-orders.scss',
})
export class MyOrders {
  private readonly orderService = inject(OrderService);

  readonly orders = signal<OrderResponse[]>([]);
  readonly loading = signal(true);
  readonly statusLabels = STATUS_LABELS;

  constructor() {
    this.orderService.listMine().subscribe({
      next: (orders) => {
        this.orders.set(orders);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
