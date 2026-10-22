import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ProductService } from '../../../core/product/product.service';
import { ProductResponse } from '../../../core/product/product.model';

const STATUS_FILTERS = [
  { value: 'EN_ATTENTE', label: 'En attente' },
  { value: 'VALIDE', label: 'Publiés' },
  { value: 'REJETE', label: 'Rejetés' },
  { value: 'DEPUBLIE', label: 'Dépubliés' },
];

@Component({
  selector: 'app-products-list',
  imports: [RouterLink, DatePipe, FormsModule],
  templateUrl: './products-list.html',
  styleUrl: './products-list.scss',
})
export class ProductsList {
  private readonly productService = inject(ProductService);

  readonly statusFilters = STATUS_FILTERS;
  readonly selectedStatus = signal<string>('EN_ATTENTE');
  readonly products = signal<ProductResponse[]>([]);
  readonly loading = signal(true);
  readonly searchQuery = signal('');

  readonly filteredProducts = computed(() => {
    const q = this.searchQuery().trim().toLowerCase();
    if (!q) return this.products();
    return this.products().filter(
      (p) => p.name.toLowerCase().includes(q) || p.shopName.toLowerCase().includes(q),
    );
  });

  constructor() {
    this.load();
  }

  selectStatus(status: string): void {
    this.selectedStatus.set(status);
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.productService.listForAdmin(this.selectedStatus()).subscribe({
      next: (page) => {
        this.products.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
