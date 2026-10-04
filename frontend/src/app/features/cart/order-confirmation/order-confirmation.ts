import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { OrderResponse } from '../../../core/order/order.model';
import { PaymentService } from '../../../core/payment/payment.service';

@Component({
  selector: 'app-order-confirmation',
  imports: [RouterLink],
  templateUrl: './order-confirmation.html',
  styleUrl: './order-confirmation.scss',
})
export class OrderConfirmation {
  private readonly router = inject(Router);
  private readonly paymentService = inject(PaymentService);

  readonly orders = signal<OrderResponse[]>([]);
  readonly paying = signal(false);
  readonly paymentError = signal<string | null>(null);

  constructor() {
    const state = this.router.getCurrentNavigation()?.extras.state ?? history.state;
    this.orders.set((state?.['orders'] as OrderResponse[]) ?? []);
  }

  payNow(): void {
    const orderIds = this.orders().map((o) => o.id);
    if (orderIds.length === 0) {
      return;
    }
    this.paying.set(true);
    this.paymentError.set(null);
    this.paymentService.initiate(orderIds).subscribe({
      next: (res) => {
        window.location.href = res.paymentUrl;
      },
      error: (err: HttpErrorResponse) => {
        this.paying.set(false);
        this.paymentError.set(err.error?.message ?? 'Impossible de démarrer le paiement, réessayez.');
      },
    });
  }
}
