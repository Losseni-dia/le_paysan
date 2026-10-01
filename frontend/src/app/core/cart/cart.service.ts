import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AddCartItemRequest, CartResponse, UpdateCartItemRequest } from './cart.model';

@Injectable({
  providedIn: 'root',
})
export class CartService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/cart`;

  readonly cart = signal<CartResponse | null>(null);

  readonly itemCount = signal(0);

  fetch(): Observable<CartResponse> {
    return this.http.get<CartResponse>(this.baseUrl).pipe(tap((cart) => this.setCart(cart)));
  }

  addItem(request: AddCartItemRequest): Observable<CartResponse> {
    return this.http.post<CartResponse>(`${this.baseUrl}/items`, request).pipe(tap((cart) => this.setCart(cart)));
  }

  updateItem(productId: number, request: UpdateCartItemRequest): Observable<CartResponse> {
    return this.http
      .put<CartResponse>(`${this.baseUrl}/items/${productId}`, request)
      .pipe(tap((cart) => this.setCart(cart)));
  }

  removeItem(productId: number): Observable<CartResponse> {
    return this.http.delete<CartResponse>(`${this.baseUrl}/items/${productId}`).pipe(tap((cart) => this.setCart(cart)));
  }

  private setCart(cart: CartResponse): void {
    this.cart.set(cart);
    this.itemCount.set(cart.items.reduce((sum, item) => sum + item.quantity, 0));
  }
}
