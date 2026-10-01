import { Component, computed, inject, signal } from '@angular/core';

import { AuthService } from '../../../core/auth/auth.service';
import { OrderService } from '../../../core/order/order.service';
import { OrderResponse } from '../../../core/order/order.model';

@Component({
  selector: 'app-user-dashboard',
  imports: [],
  templateUrl: './user-dashboard.html',
  styleUrl: './user-dashboard.scss',
})
export class UserDashboard {
  readonly authService = inject(AuthService);
  private readonly orderService = inject(OrderService);

  readonly loading = signal(true);
  readonly orders = signal<OrderResponse[]>([]);

  readonly totalOrders = computed(() => this.orders().length);

  readonly totalSpent = computed(() =>
    this.orders()
      .filter((o) => o.status !== 'ANNULEE' && o.status !== 'EN_ATTENTE_PAIEMENT')
      .reduce((sum, o) => sum + o.totalAmount, 0),
  );

  readonly pendingPaymentCount = computed(
    () => this.orders().filter((o) => o.status === 'EN_ATTENTE_PAIEMENT').length,
  );

  readonly deliveredCount = computed(() => this.orders().filter((o) => o.status === 'LIVREE').length);

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
