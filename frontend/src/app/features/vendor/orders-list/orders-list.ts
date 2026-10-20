import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { OrderService } from '../../../core/order/order.service';
import { AdminOrderResponse, BulkAssignLivreurResult } from '../../../core/order/order.model';
import { LivreurService } from '../../../core/livreur/livreur.service';
import { LivreurResponse } from '../../../core/livreur/livreur.model';

const STATUS_FILTERS = [
  { value: '', label: 'Toutes' },
  { value: 'PAYEE', label: 'Payées' },
  { value: 'EN_PREPARATION', label: 'En préparation' },
  { value: 'PRETE', label: 'Prêtes' },
  { value: 'EXPEDIEE', label: 'Expédiées' },
  { value: 'LIVREE', label: 'Livrées' },
  { value: 'ANNULEE', label: 'Annulées' },
];

@Component({
  selector: 'app-vendor-orders-list',
  imports: [RouterLink, DatePipe, FormsModule],
  templateUrl: './orders-list.html',
  styleUrl: './orders-list.scss',
})
export class OrdersList {
  private readonly orderService = inject(OrderService);
  private readonly livreurService = inject(LivreurService);

  readonly statusFilters = STATUS_FILTERS;
  readonly selectedStatus = signal('');
  readonly orders = signal<AdminOrderResponse[]>([]);
  readonly loading = signal(true);

  readonly livreurs = signal<LivreurResponse[]>([]);
  readonly selectedOrderIds = signal<Set<number>>(new Set());
  readonly bulkLivreurId = signal<number | null>(null);
  readonly bulkAssigning = signal(false);
  readonly bulkResults = signal<BulkAssignLivreurResult[] | null>(null);

  constructor() {
    this.load();
    this.livreurService.listForShop().subscribe({ next: (livreurs) => this.livreurs.set(livreurs) });
  }

  selectStatus(status: string): void {
    this.selectedStatus.set(status);
    this.selectedOrderIds.set(new Set());
    this.bulkResults.set(null);
    this.load();
  }

  isAssignable(order: AdminOrderResponse): boolean {
    return order.status === 'EXPEDIEE';
  }

  isSelected(orderId: number): boolean {
    return this.selectedOrderIds().has(orderId);
  }

  toggleSelection(orderId: number): void {
    const next = new Set(this.selectedOrderIds());
    if (next.has(orderId)) {
      next.delete(orderId);
    } else {
      next.add(orderId);
    }
    this.selectedOrderIds.set(next);
  }

  toggleSelectAllAssignable(): void {
    const assignableIds = this.orders().filter((o) => this.isAssignable(o)).map((o) => o.id);
    const allSelected = assignableIds.length > 0 && assignableIds.every((id) => this.selectedOrderIds().has(id));
    this.selectedOrderIds.set(allSelected ? new Set() : new Set(assignableIds));
  }

  confirmBulkAssign(): void {
    const livreurId = this.bulkLivreurId();
    const orderIds = [...this.selectedOrderIds()];
    if (!livreurId || orderIds.length === 0) return;

    this.bulkAssigning.set(true);
    this.bulkResults.set(null);
    this.orderService.bulkAssignLivreur(orderIds, livreurId).subscribe({
      next: (results) => {
        this.bulkAssigning.set(false);
        this.bulkResults.set(results);
        this.selectedOrderIds.set(new Set());
        this.load();
      },
      error: () => this.bulkAssigning.set(false),
    });
  }

  private load(): void {
    this.loading.set(true);
    this.orderService.listForVendor(this.selectedStatus() || undefined).subscribe({
      next: (page) => {
        this.orders.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
