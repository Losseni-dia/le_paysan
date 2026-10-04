import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PageResponse } from '../shop/shop.model';
import { VendorPayoutResponse } from './payout.model';

@Injectable({
  providedIn: 'root',
})
export class PayoutService {
  private readonly http = inject(HttpClient);
  private readonly adminUrl = `${environment.apiBaseUrl}/admin/payouts`;

  listForAdmin(status?: string, page = 0): Observable<PageResponse<VendorPayoutResponse>> {
    let params = new HttpParams().set('page', page);
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<PageResponse<VendorPayoutResponse>>(this.adminUrl, { params });
  }

  retry(id: number): Observable<void> {
    return this.http.post<void>(`${this.adminUrl}/${id}/retry`, {});
  }
}
