import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PageResponse } from '../shop/shop.model';
import { ProductResponse } from './product.model';

export interface PublicProductFilters {
  categoryId?: number;
  shopId?: number;
  minPrice?: number;
  maxPrice?: number;
  deliveryMode?: string;
  search?: string;
  page?: number;
}

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly publicUrl = `${environment.apiBaseUrl}/products`;
  private readonly vendorUrl = `${environment.apiBaseUrl}/vendor/products`;
  private readonly adminUrl = `${environment.apiBaseUrl}/admin/products`;

  listPublic(filters: PublicProductFilters): Observable<PageResponse<ProductResponse>> {
    let params = new HttpParams();
    if (filters.categoryId != null) params = params.set('categoryId', filters.categoryId);
    if (filters.shopId != null) params = params.set('shopId', filters.shopId);
    if (filters.minPrice != null) params = params.set('minPrice', filters.minPrice);
    if (filters.maxPrice != null) params = params.set('maxPrice', filters.maxPrice);
    if (filters.deliveryMode) params = params.set('deliveryMode', filters.deliveryMode);
    if (filters.search) params = params.set('search', filters.search);
    if (filters.page != null) params = params.set('page', filters.page);
    return this.http.get<PageResponse<ProductResponse>>(this.publicUrl, { params });
  }

  getPublic(id: number): Observable<ProductResponse> {
    return this.http.get<ProductResponse>(`${this.publicUrl}/${id}`);
  }

  // --- Vendeur ---

  create(formData: FormData): Observable<ProductResponse> {
    return this.http.post<ProductResponse>(this.vendorUrl, formData);
  }

  update(id: number, formData: FormData): Observable<ProductResponse> {
    return this.http.put<ProductResponse>(`${this.vendorUrl}/${id}`, formData);
  }

  togglePublishState(id: number): Observable<ProductResponse> {
    return this.http.patch<ProductResponse>(`${this.vendorUrl}/${id}/publish-state`, {});
  }

  listMine(page = 0, size = 50): Observable<PageResponse<ProductResponse>> {
    return this.http.get<PageResponse<ProductResponse>>(this.vendorUrl, { params: { page, size } });
  }

  // --- Admin ---

  listForAdmin(status?: string, page = 0): Observable<PageResponse<ProductResponse>> {
    let params = new HttpParams().set('page', page);
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<PageResponse<ProductResponse>>(this.adminUrl, { params });
  }

  getAdminDetail(id: number): Observable<ProductResponse> {
    return this.http.get<ProductResponse>(`${this.adminUrl}/${id}`);
  }

  validate(id: number): Observable<ProductResponse> {
    return this.http.post<ProductResponse>(`${this.adminUrl}/${id}/validate`, {});
  }

  reject(id: number, reason: string): Observable<ProductResponse> {
    return this.http.post<ProductResponse>(`${this.adminUrl}/${id}/reject`, { reason });
  }
}
