import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';
import { OrderService } from '../../../core/order/order.service';
import { OrderResponse } from '../../../core/order/order.model';

@Component({
  selector: 'app-order-invoice',
  imports: [DatePipe],
  templateUrl: './order-invoice.html',
  styleUrl: './order-invoice.scss',
})
export class OrderInvoice {
  private readonly route = inject(ActivatedRoute);
  private readonly orderService = inject(OrderService);
  readonly authService = inject(AuthService);

  readonly order = signal<OrderResponse | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.orderService.getMine(id).subscribe({
      next: (order) => {
        this.order.set(order);
        this.loading.set(false);
      },
      error: () => {
        this.error.set("Impossible de charger cette facture.");
        this.loading.set(false);
      },
    });
  }

  print(): void {
    window.print();
  }
}
