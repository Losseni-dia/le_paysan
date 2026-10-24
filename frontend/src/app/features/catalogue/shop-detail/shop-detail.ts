import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { ShopService } from '../../../core/shop/shop.service';
import { ShopResponse } from '../../../core/shop/shop.model';
import { ProductService } from '../../../core/product/product.service';
import { ProductResponse } from '../../../core/product/product.model';
import { MapPicker } from '../../../shared/ui/map-picker/map-picker';
import { ReviewSection } from '../../../shared/ui/review-section/review-section';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-shop-detail',
  imports: [RouterLink, MapPicker, ReviewSection],
  templateUrl: './shop-detail.html',
  styleUrl: './shop-detail.scss',
})
export class ShopDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly shopService = inject(ShopService);
  private readonly productService = inject(ProductService);

  readonly shop = signal<ShopResponse | null>(null);
  readonly products = signal<ProductResponse[]>([]);
  readonly loading = signal(true);
  readonly notFound = signal(false);
  readonly apiFilesBase = `${environment.apiBaseUrl}/files/`;

  constructor() {
    const slug = this.route.snapshot.paramMap.get('slug') ?? '';
    this.shopService.getPublicShop(slug).subscribe({
      next: (shop) => {
        this.shop.set(shop);
        this.loading.set(false);
        this.productService.listPublic({ shopId: shop.id }).subscribe((page) => this.products.set(page.content));
      },
      error: () => {
        this.notFound.set(true);
        this.loading.set(false);
      },
    });
  }

  itineraryUrl(): string {
    const shop = this.shop();
    if (!shop) {
      return '';
    }
    return `https://www.openstreetmap.org/directions?to=${shop.latitude}%2C${shop.longitude}`;
  }
}
