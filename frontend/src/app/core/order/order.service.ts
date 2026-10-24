import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PageResponse } from '../shop/shop.model';
import { AdminOrderResponse, OrderResponse } from './order.model';

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/orders`;
  private readonly adminUrl = `${environment.apiBaseUrl}/admin/orders`;
  private readonly vendorUrl = `${environment.apiBaseUrl}/vendor/orders`;

  checkout(): Observable<OrderResponse[]> {
    return this.http.post<OrderResponse[]>(this.baseUrl, {});
  }

  listMine(): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(this.baseUrl);
  }

  getMine(id: number): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(`${this.baseUrl}/${id}`);
  }

  confirmReception(id: number): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${this.baseUrl}/${id}/confirmer-reception`, {});
  }

  // --- Admin ---

  listForAdmin(status?: string, page = 0): Observable<PageResponse<AdminOrderResponse>> {
    let params = new HttpParams().set('page', page);
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<PageResponse<AdminOrderResponse>>(this.adminUrl, { params });
  }

  getAdminDetail(id: number): Observable<AdminOrderResponse> {
    return this.http.get<AdminOrderResponse>(`${this.adminUrl}/${id}`);
  }

  // --- Vendeur ---

  listForVendor(status?: string, page = 0): Observable<PageResponse<AdminOrderResponse>> {
    let params = new HttpParams().set('page', page);
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<PageResponse<AdminOrderResponse>>(this.vendorUrl, { params });
  }

  getForVendor(id: number): Observable<AdminOrderResponse> {
    return this.http.get<AdminOrderResponse>(`${this.vendorUrl}/${id}`);
  }

  markNextStatus(
    id: number,
    options?: { proofType?: 'SIGNATURE' | 'PHOTO'; proof?: Blob },
  ): Observable<AdminOrderResponse> {
    const formData = new FormData();
    if (options?.proofType && options.proof) {
      formData.append('proofType', options.proofType);
      formData.append('proof', options.proof, options.proofType === 'SIGNATURE' ? 'signature.png' : 'photo.jpg');
    }
    return this.http.patch<AdminOrderResponse>(`${this.vendorUrl}/${id}/status`, formData);
  }
}
