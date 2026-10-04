import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';

import { OrderService } from '../../../core/order/order.service';
import { AdminOrderResponse } from '../../../core/order/order.model';
import { OrderTimeline } from '../../../shared/ui/order-timeline/order-timeline';

@Component({
  selector: 'app-admin-order-detail',
  imports: [RouterLink, DatePipe, OrderTimeline],
  templateUrl: './order-detail.html',
  styleUrl: './order-detail.scss',
})
export class OrderDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly orderService = inject(OrderService);

  readonly order = signal<AdminOrderResponse | null>(null);
  readonly loading = signal(true);

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.orderService.getAdminDetail(id).subscribe({
      next: (order) => {
        this.order.set(order);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
