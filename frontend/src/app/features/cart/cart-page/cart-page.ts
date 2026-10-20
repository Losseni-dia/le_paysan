import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { CartService } from '../../../core/cart/cart.service';
import { CartItemResponse, DeliveryMode } from '../../../core/cart/cart.model';
import { OrderService } from '../../../core/order/order.service';
import { CI_CITIES } from '../../../core/catalogue/ci-cities';
import { environment } from '../../../../environments/environment';

interface ShopGroup {
  shopId: number;
  shopName: string;
  items: CartItemResponse[];
}

@Component({
  selector: 'app-cart-page',
  imports: [RouterLink],
  templateUrl: './cart-page.html',
  styleUrl: './cart-page.scss',
})
export class CartPage {
  private readonly cartService = inject(CartService);
  private readonly orderService = inject(OrderService);
  private readonly router = inject(Router);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly checkingOut = signal(false);
  readonly apiFilesBase = `${environment.apiBaseUrl}/files/`;

  readonly cart = this.cartService.cart;

  readonly shopGroups = computed<ShopGroup[]>(() => {
    const cart = this.cart();
    if (!cart) {
      return [];
    }
    const map = new Map<number, ShopGroup>();
    for (const item of cart.items) {
      if (!map.has(item.shopId)) {
        map.set(item.shopId, { shopId: item.shopId, shopName: item.shopName, items: [] });
      }
      map.get(item.shopId)!.items.push(item);
    }
    return [...map.values()];
  });

  readonly hasUnavailableItems = computed(() => this.cart()?.items.some((i) => !i.available) ?? false);
  readonly cities = CI_CITIES;

  constructor() {
    this.cartService.fetch().subscribe({
      next: () => this.loading.set(false),
      error: () => this.loading.set(false),
    });
  }

  updateQuantity(item: CartItemResponse, event: Event): void {
    const input = event.target as HTMLInputElement;
    const quantity = Number(input.value);
    if (!quantity || quantity < 1) {
      return;
    }
    this.error.set(null);
    this.cartService
      .updateItem(item.productId, {
        quantity,
        deliveryMode: item.deliveryMode,
        deliveryAddress: item.deliveryAddress ?? undefined,
        deliveryCityName: item.deliveryCityName ?? undefined,
        deliveryCityLat: item.deliveryCityLat ?? undefined,
        deliveryCityLng: item.deliveryCityLng ?? undefined,
      })
      .subscribe({ error: (err: HttpErrorResponse) => this.error.set(err.error?.message ?? 'Erreur de mise à jour.') });
  }

  updateDeliveryMode(item: CartItemResponse, event: Event): void {
    const select = event.target as HTMLSelectElement;
    const deliveryMode = select.value as DeliveryMode;
    this.error.set(null);
    this.cartService
      .updateItem(item.productId, {
        quantity: item.quantity,
        deliveryMode,
        deliveryAddress: deliveryMode === 'LIVRAISON_DOMICILE' ? (item.deliveryAddress ?? '') : undefined,
        deliveryCityName: item.deliveryCityName ?? undefined,
        deliveryCityLat: item.deliveryCityLat ?? undefined,
        deliveryCityLng: item.deliveryCityLng ?? undefined,
      })
      .subscribe({ error: (err: HttpErrorResponse) => this.error.set(err.error?.message ?? 'Erreur de mise à jour.') });
  }

  updateAddress(item: CartItemResponse, event: Event): void {
    const input = event.target as HTMLInputElement;
    const deliveryAddress = input.value;
    this.error.set(null);
    this.cartService
      .updateItem(item.productId, {
        quantity: item.quantity,
        deliveryMode: item.deliveryMode,
        deliveryAddress,
        deliveryCityName: item.deliveryCityName ?? undefined,
        deliveryCityLat: item.deliveryCityLat ?? undefined,
        deliveryCityLng: item.deliveryCityLng ?? undefined,
      })
      .subscribe({ error: (err: HttpErrorResponse) => this.error.set(err.error?.message ?? 'Erreur de mise à jour.') });
  }

  updateCity(item: CartItemResponse, event: Event): void {
    const select = event.target as HTMLSelectElement;
    const city = this.cities.find((c) => c.name === select.value);
    if (!city) return;
    this.error.set(null);
    this.cartService
      .updateItem(item.productId, {
        quantity: item.quantity,
        deliveryMode: item.deliveryMode,
        deliveryAddress: item.deliveryAddress ?? undefined,
        deliveryCityName: city.name,
        deliveryCityLat: city.lat,
        deliveryCityLng: city.lng,
      })
      .subscribe({ error: (err: HttpErrorResponse) => this.error.set(err.error?.message ?? 'Erreur de mise à jour.') });
  }

  removeItem(item: CartItemResponse): void {
    this.cartService.removeItem(item.productId).subscribe({
      error: (err: HttpErrorResponse) => this.error.set(err.error?.message ?? 'Erreur lors de la suppression.'),
    });
  }

  checkout(): void {
    this.checkingOut.set(true);
    this.error.set(null);
    this.orderService.checkout().subscribe({
      next: (orders) => {
        this.checkingOut.set(false);
        this.router.navigate(['/commandes/confirmation'], { state: { orders } });
      },
      error: (err: HttpErrorResponse) => {
        this.checkingOut.set(false);
        this.error.set(err.error?.message ?? 'Impossible de finaliser la commande.');
        this.cartService.fetch().subscribe();
      },
    });
  }
}
