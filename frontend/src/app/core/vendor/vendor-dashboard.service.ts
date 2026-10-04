import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { VendorDashboardResponse, VendorStatsResponse } from './vendor-dashboard.model';

@Injectable({
  providedIn: 'root',
})
export class VendorDashboardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/vendor`;

  getDashboard(): Observable<VendorDashboardResponse> {
    return this.http.get<VendorDashboardResponse>(`${this.baseUrl}/dashboard`);
  }

  getStats(): Observable<VendorStatsResponse> {
    return this.http.get<VendorStatsResponse>(`${this.baseUrl}/statistiques`);
  }
}
