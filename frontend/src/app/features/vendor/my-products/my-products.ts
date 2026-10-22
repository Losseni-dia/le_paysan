import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { ProductService } from '../../../core/product/product.service';
import { ProductResponse } from '../../../core/product/product.model';
import { environment } from '../../../../environments/environment';

const STATUS_LABELS: Record<string, string> = {
  EN_ATTENTE: 'En attente de validation',
  VALIDE: 'Publié',
  REJETE: 'Rejeté',
  DEPUBLIE: 'Dépublié',
};

@Component({
  selector: 'app-my-products',
  imports: [RouterLink, FormsModule],
  templateUrl: './my-products.html',
  styleUrl: './my-products.scss',
})
export class MyProducts {
  private readonly productService = inject(ProductService);

  readonly products = signal<ProductResponse[]>([]);
  readonly loading = signal(true);
  readonly statusLabels = STATUS_LABELS;
  readonly apiFilesBase = `${environment.apiBaseUrl}/files/`;
  readonly togglingId = signal<number | null>(null);
  readonly searchQuery = signal('');

  readonly filteredProducts = computed(() => {
    const q = this.searchQuery().trim().toLowerCase();
    if (!q) return this.products();
    return this.products().filter((p) => p.name.toLowerCase().includes(q));
  });

  constructor() {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.productService.listMine().subscribe({
      next: (page) => {
        this.products.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  togglePublish(product: ProductResponse): void {
    this.togglingId.set(product.id);
    this.productService.togglePublishState(product.id).subscribe({
      next: (updated) => {
        this.products.update((list) => list.map((p) => (p.id === updated.id ? updated : p)));
        this.togglingId.set(null);
      },
      error: () => this.togglingId.set(null),
    });
  }
}
