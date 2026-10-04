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

  constructor() {
    this.livreurService.listMyDeliveries().subscribe({
      next: (page) => {
        this.orders.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
