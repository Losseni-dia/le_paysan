import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PageResponse } from '../shop/shop.model';
import {
  ProductReviewResponse,
  RatingSummaryResponse,
  ShopReviewResponse,
  SubmitReviewRequest,
} from './review.model';

@Injectable({
  providedIn: 'root',
})
export class ReviewService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  // --- Produit ---

  listForProduct(productId: number, page = 0): Observable<PageResponse<ProductReviewResponse>> {
    return this.http.get<PageResponse<ProductReviewResponse>>(`${this.base}/products/${productId}/avis`, {
      params: { page },
    });
  }

  getProductSummary(productId: number): Observable<RatingSummaryResponse> {
    return this.http.get<RatingSummaryResponse>(`${this.base}/products/${productId}/avis/resume`);
  }

  getMyProductReview(productId: number): Observable<ProductReviewResponse | null> {
    return this.http.get<ProductReviewResponse>(`${this.base}/products/${productId}/avis/moi`, {
      observe: 'body',
    });
  }

  submitProductReview(productId: number, request: SubmitReviewRequest): Observable<ProductReviewResponse> {
    return this.http.post<ProductReviewResponse>(`${this.base}/products/${productId}/avis`, request);
  }

  // --- Boutique ---

  listForShop(shopId: number, page = 0): Observable<PageResponse<ShopReviewResponse>> {
    return this.http.get<PageResponse<ShopReviewResponse>>(`${this.base}/shops/${shopId}/avis`, { params: { page } });
  }

  getShopSummary(shopId: number): Observable<RatingSummaryResponse> {
    return this.http.get<RatingSummaryResponse>(`${this.base}/shops/${shopId}/avis/resume`);
  }

  getMyShopReview(shopId: number): Observable<ShopReviewResponse | null> {
    return this.http.get<ShopReviewResponse>(`${this.base}/shops/${shopId}/avis/moi`, { observe: 'body' });
  }

  submitShopReview(shopId: number, request: SubmitReviewRequest): Observable<ShopReviewResponse> {
    return this.http.post<ShopReviewResponse>(`${this.base}/shops/${shopId}/avis`, request);
  }

  // --- Admin ---

  listProductReviewsForAdmin(status?: string, page = 0): Observable<PageResponse<ProductReviewResponse>> {
    let params = new HttpParams().set('page', page);
    if (status) params = params.set('status', status);
    return this.http.get<PageResponse<ProductReviewResponse>>(`${this.base}/admin/avis/produits`, { params });
  }

  listShopReviewsForAdmin(status?: string, page = 0): Observable<PageResponse<ShopReviewResponse>> {
    let params = new HttpParams().set('page', page);
    if (status) params = params.set('status', status);
    return this.http.get<PageResponse<ShopReviewResponse>>(`${this.base}/admin/avis/boutiques`, { params });
  }

  moderateProductReview(id: number, status: string, reason?: string): Observable<ProductReviewResponse> {
    return this.http.patch<ProductReviewResponse>(`${this.base}/admin/avis/produits/${id}`, { status, reason });
  }

  moderateShopReview(id: number, status: string, reason?: string): Observable<ShopReviewResponse> {
    return this.http.patch<ShopReviewResponse>(`${this.base}/admin/avis/boutiques/${id}`, { status, reason });
  }
}
