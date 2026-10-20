import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PageResponse } from '../shop/shop.model';
import { AdminOrderResponse } from '../order/order.model';
import { CreateLivreurRequest, LivreurAdminResponse, LivreurResponse, UpdateLivreurRequest } from './livreur.model';

@Injectable({
  providedIn: 'root',
})
export class LivreurService {
  private readonly http = inject(HttpClient);
  private readonly vendorUrl = `${environment.apiBaseUrl}/vendor/livreurs`;
  private readonly livreurOrdersUrl = `${environment.apiBaseUrl}/livreur/orders`;
  private readonly adminUrl = `${environment.apiBaseUrl}/admin/livreurs`;

  // --- Vendeur ---

  listForShop(): Observable<LivreurResponse[]> {
    return this.http.get<LivreurResponse[]>(this.vendorUrl);
  }

  create(request: CreateLivreurRequest): Observable<LivreurResponse> {
    const formData = new FormData();
    formData.append('username', request.username);
    formData.append('password', request.password);
    formData.append('firstName', request.firstName);
    formData.append('lastName', request.lastName);
    if (request.phone) formData.append('phone', request.phone);
    if (request.city) formData.append('city', request.city);
    if (request.photo) formData.append('photo', request.photo);
    if (request.idDocumentFront) formData.append('idDocumentFront', request.idDocumentFront);
    if (request.idDocumentBack) formData.append('idDocumentBack', request.idDocumentBack);
    if (request.selfieWithId) formData.append('selfieWithId', request.selfieWithId);
    return this.http.post<LivreurResponse>(this.vendorUrl, formData);
  }

  update(id: number, request: UpdateLivreurRequest): Observable<LivreurResponse> {
    const formData = new FormData();
    formData.append('firstName', request.firstName);
    formData.append('lastName', request.lastName);
    if (request.phone) formData.append('phone', request.phone);
    if (request.city) formData.append('city', request.city);
    if (request.photo) formData.append('photo', request.photo);
    if (request.idDocumentFront) formData.append('idDocumentFront', request.idDocumentFront);
    if (request.idDocumentBack) formData.append('idDocumentBack', request.idDocumentBack);
    if (request.selfieWithId) formData.append('selfieWithId', request.selfieWithId);
    return this.http.patch<LivreurResponse>(`${this.vendorUrl}/${id}`, formData);
  }

  setEnabled(id: number, enabled: boolean): Observable<LivreurResponse> {
    return this.http.patch<LivreurResponse>(`${this.vendorUrl}/${id}/enabled`, { enabled });
  }

  resetPassword(id: number, password: string): Observable<LivreurResponse> {
    return this.http.patch<LivreurResponse>(`${this.vendorUrl}/${id}/mot-de-passe`, { password });
  }

  // --- Livreur ---

  listMyDeliveries(page = 0): Observable<PageResponse<AdminOrderResponse>> {
    return this.http.get<PageResponse<AdminOrderResponse>>(this.livreurOrdersUrl, { params: { page } });
  }

  listMyDeliveryHistory(page = 0): Observable<PageResponse<AdminOrderResponse>> {
    return this.http.get<PageResponse<AdminOrderResponse>>(this.livreurOrdersUrl, {
      params: { page, status: 'LIVREE' },
    });
  }

  getDelivery(orderId: number): Observable<AdminOrderResponse> {
    return this.http.get<AdminOrderResponse>(`${this.livreurOrdersUrl}/${orderId}`);
  }

  markPickedUp(orderId: number): Observable<AdminOrderResponse> {
    return this.http.post<AdminOrderResponse>(`${this.livreurOrdersUrl}/${orderId}/recuperer`, {});
  }

  markEnRoute(orderId: number): Observable<AdminOrderResponse> {
    return this.http.post<AdminOrderResponse>(`${this.livreurOrdersUrl}/${orderId}/en-route`, {});
  }

  confirmDelivery(orderId: number, proofType: 'SIGNATURE' | 'PHOTO', proof: Blob): Observable<AdminOrderResponse> {
    const formData = new FormData();
    formData.append('proofType', proofType);
    formData.append('proof', proof, proofType === 'SIGNATURE' ? 'signature.png' : 'photo.jpg');
    return this.http.post<AdminOrderResponse>(`${this.livreurOrdersUrl}/${orderId}/confirmer-remise`, formData);
  }

  getMe(): Observable<LivreurResponse> {
    return this.http.get<LivreurResponse>(`${environment.apiBaseUrl}/livreur/moi`);
  }

  setAvailability(available: boolean): Observable<LivreurResponse> {
    return this.http.patch<LivreurResponse>(`${environment.apiBaseUrl}/livreur/disponibilite`, { available });
  }

  // --- Admin ---

  listForAdmin(kycStatus?: string, page = 0): Observable<PageResponse<LivreurAdminResponse>> {
    let params = new HttpParams().set('page', page);
    if (kycStatus) params = params.set('kycStatus', kycStatus);
    return this.http.get<PageResponse<LivreurAdminResponse>>(this.adminUrl, { params });
  }

  getAdminDetail(id: number): Observable<LivreurAdminResponse> {
    return this.http.get<LivreurAdminResponse>(`${this.adminUrl}/${id}`);
  }

  validateKyc(id: number): Observable<LivreurAdminResponse> {
    return this.http.post<LivreurAdminResponse>(`${this.adminUrl}/${id}/valider-kyc`, {});
  }

  rejectKyc(id: number, reason: string): Observable<LivreurAdminResponse> {
    return this.http.post<LivreurAdminResponse>(`${this.adminUrl}/${id}/rejeter-kyc`, { reason });
  }
}
