import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  PageResponse,
  PayoutAccountResponse,
  ShopAdminDetailResponse,
  ShopDeliveryCity,
  ShopDeliveryTier,
  ShopResponse,
} from './shop.model';

@Injectable({
  providedIn: 'root',
})
export class ShopService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/shops`;
  private readonly adminUrl = `${environment.apiBaseUrl}/admin/shops`;

  submitRequest(formData: FormData): Observable<ShopResponse> {
    return this.http.post<ShopResponse>(`${this.baseUrl}/requests`, formData);
  }

  getMyShop(): Observable<ShopResponse> {
    return this.http.get<ShopResponse>(`${this.baseUrl}/me`);
  }

  updateMyShop(payload: {
    description: string;
    category: string;
    addressCity: string;
    addressDetail: string;
    latitude: number;
    longitude: number;
    phone: string;
  }): Observable<ShopResponse> {
    return this.http.put<ShopResponse>(`${this.baseUrl}/me`, payload);
  }

  getPayoutAccount(): Observable<PayoutAccountResponse> {
    return this.http.get<PayoutAccountResponse>(`${this.baseUrl}/me/payout-account`);
  }

  upsertPayoutAccount(payload: {
    operator: string;
    phoneNumber: string;
    accountHolderName: string;
  }): Observable<PayoutAccountResponse> {
    return this.http.put<PayoutAccountResponse>(`${this.baseUrl}/me/payout-account`, payload);
  }

  getPublicShop(id: number): Observable<ShopResponse> {
    return this.http.get<ShopResponse>(`${this.baseUrl}/${id}`);
  }

  listPublicShops(search?: string): Observable<PageResponse<ShopResponse>> {
    let params = new HttpParams();
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<PageResponse<ShopResponse>>(this.baseUrl, { params });
  }

  getPublicDeliveryTiers(shopId: number): Observable<ShopDeliveryTier[]> {
    return this.http.get<ShopDeliveryTier[]>(`${this.baseUrl}/${shopId}/paliers-livraison`);
  }

  getMyDeliveryTiers(): Observable<ShopDeliveryTier[]> {
    return this.http.get<ShopDeliveryTier[]>(`${environment.apiBaseUrl}/vendor/shop/paliers-livraison`);
  }

  saveMyDeliveryTiers(tiers: ShopDeliveryTier[]): Observable<ShopDeliveryTier[]> {
    return this.http.put<ShopDeliveryTier[]>(
      `${environment.apiBaseUrl}/vendor/shop/paliers-livraison`,
      tiers.map((t) => ({ maxDistanceKm: t.maxDistanceKm, price: t.price })),
    );
  }

  getPublicDeliveryCities(shopId: number): Observable<ShopDeliveryCity[]> {
    return this.http.get<ShopDeliveryCity[]>(`${this.baseUrl}/${shopId}/villes-livraison`);
  }

  getMyDeliveryCities(): Observable<ShopDeliveryCity[]> {
    return this.http.get<ShopDeliveryCity[]>(`${environment.apiBaseUrl}/vendor/shop/villes-livraison`);
  }

  saveMyDeliveryCities(cities: ShopDeliveryCity[]): Observable<ShopDeliveryCity[]> {
    return this.http.put<ShopDeliveryCity[]>(
      `${environment.apiBaseUrl}/vendor/shop/villes-livraison`,
      cities.map((c) => ({ cityName: c.cityName, price: c.price })),
    );
  }

  // --- Admin ---

  listForAdmin(status?: string, page = 0): Observable<PageResponse<ShopAdminDetailResponse>> {
    let params = new HttpParams().set('page', page);
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<PageResponse<ShopAdminDetailResponse>>(this.adminUrl, { params });
  }

  getAdminDetail(id: number): Observable<ShopAdminDetailResponse> {
    return this.http.get<ShopAdminDetailResponse>(`${this.adminUrl}/${id}`);
  }

  validate(id: number): Observable<ShopResponse> {
    return this.http.post<ShopResponse>(`${this.adminUrl}/${id}/validate`, {});
  }

  reject(id: number, reason: string): Observable<ShopResponse> {
    return this.http.post<ShopResponse>(`${this.adminUrl}/${id}/reject`, { reason });
  }

  suspend(id: number): Observable<ShopResponse> {
    return this.http.post<ShopResponse>(`${this.adminUrl}/${id}/suspend`, {});
  }
}
