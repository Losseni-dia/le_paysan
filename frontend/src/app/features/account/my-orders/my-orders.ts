import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';

import { OrderService } from '../../../core/order/order.service';
import { OrderResponse } from '../../../core/order/order.model';
import { PaymentService } from '../../../core/payment/payment.service';
import { OrderTimeline } from '../../../shared/ui/order-timeline/order-timeline';

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
  imports: [RouterLink, OrderTimeline],
  templateUrl: './my-orders.html',
  styleUrl: './my-orders.scss',
})
export class MyOrders {
  private readonly orderService = inject(OrderService);
  private readonly paymentService = inject(PaymentService);

  readonly orders = signal<OrderResponse[]>([]);
  readonly loading = signal(true);
  readonly statusLabels = STATUS_LABELS;
  readonly payingId = signal<number | null>(null);
  readonly paymentError = signal<string | null>(null);
  readonly confirmingId = signal<number | null>(null);

  constructor() {
    this.orderService.listMine().subscribe({
      next: (orders) => {
        this.orders.set(orders);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  confirmReception(order: OrderResponse): void {
    this.confirmingId.set(order.id);
    this.orderService.confirmReception(order.id).subscribe({
      next: (updated) => {
        this.orders.set(this.orders().map((o) => (o.id === updated.id ? updated : o)));
        this.confirmingId.set(null);
      },
      error: () => this.confirmingId.set(null),
    });
  }

  payNow(order: OrderResponse): void {
    this.payingId.set(order.id);
    this.paymentError.set(null);
    this.paymentService.initiate([order.id]).subscribe({
      next: (res) => {
        window.location.href = res.paymentUrl;
      },
      error: (err: HttpErrorResponse) => {
        this.payingId.set(null);
        this.paymentError.set(err.error?.message ?? 'Impossible de démarrer le paiement, réessayez.');
      },
    });
  }
}
