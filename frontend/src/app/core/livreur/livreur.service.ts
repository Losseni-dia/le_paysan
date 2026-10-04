import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PageResponse } from '../shop/shop.model';
import { AdminOrderResponse } from '../order/order.model';
import { CreateLivreurRequest, LivreurResponse } from './livreur.model';

@Injectable({
  providedIn: 'root',
})
export class LivreurService {
  private readonly http = inject(HttpClient);
  private readonly vendorUrl = `${environment.apiBaseUrl}/vendor/livreurs`;
  private readonly livreurOrdersUrl = `${environment.apiBaseUrl}/livreur/orders`;

  // --- Vendeur ---

  listForShop(): Observable<LivreurResponse[]> {
    return this.http.get<LivreurResponse[]>(this.vendorUrl);
  }

  create(request: CreateLivreurRequest): Observable<LivreurResponse> {
    return this.http.post<LivreurResponse>(this.vendorUrl, request);
  }

  // --- Livreur ---

  listMyDeliveries(page = 0): Observable<PageResponse<AdminOrderResponse>> {
    return this.http.get<PageResponse<AdminOrderResponse>>(this.livreurOrdersUrl, { params: { page } });
  }

  getDelivery(orderId: number): Observable<AdminOrderResponse> {
    return this.http.get<AdminOrderResponse>(`${this.livreurOrdersUrl}/${orderId}`);
  }

  confirmDelivery(orderId: number, proofType: 'SIGNATURE' | 'PHOTO', proof: Blob): Observable<AdminOrderResponse> {
    const formData = new FormData();
    formData.append('proofType', proofType);
    formData.append('proof', proof, proofType === 'SIGNATURE' ? 'signature.png' : 'photo.jpg');
    return this.http.post<AdminOrderResponse>(`${this.livreurOrdersUrl}/${orderId}/confirmer-remise`, formData);
  }
}
