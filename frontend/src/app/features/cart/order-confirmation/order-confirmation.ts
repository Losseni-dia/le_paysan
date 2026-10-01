import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { OrderResponse } from '../../../core/order/order.model';

@Component({
  selector: 'app-order-confirmation',
  imports: [RouterLink],
  templateUrl: './order-confirmation.html',
  styleUrl: './order-confirmation.scss',
})
export class OrderConfirmation {
  private readonly router = inject(Router);

  readonly orders = signal<OrderResponse[]>([]);

  constructor() {
    const state = this.router.getCurrentNavigation()?.extras.state ?? history.state;
    this.orders.set((state?.['orders'] as OrderResponse[]) ?? []);
  }
}
