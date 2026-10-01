import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { ProductService } from '../../../core/product/product.service';
import { ProductResponse } from '../../../core/product/product.model';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-product-detail',
  imports: [RouterLink],
  templateUrl: './product-detail.html',
  styleUrl: './product-detail.scss',
})
export class ProductDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly productService = inject(ProductService);

  readonly product = signal<ProductResponse | null>(null);
  readonly loading = signal(true);
  readonly notFound = signal(false);
  readonly activeImage = signal(0);
  readonly apiFilesBase = `${environment.apiBaseUrl}/files/`;

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.productService.getPublic(id).subscribe({
      next: (p) => {
        this.product.set(p);
        this.loading.set(false);
      },
      error: () => {
        this.notFound.set(true);
        this.loading.set(false);
      },
    });
  }

  selectImage(index: number): void {
    this.activeImage.set(index);
  }
}
