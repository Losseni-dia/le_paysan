import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CommissionRateResponse } from './commission.model';

@Injectable({
  providedIn: 'root',
})
export class CommissionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin/commission`;

  getCurrent(): Observable<CommissionRateResponse> {
    return this.http.get<CommissionRateResponse>(this.baseUrl);
  }

  getHistory(): Observable<CommissionRateResponse[]> {
    return this.http.get<CommissionRateResponse[]>(`${this.baseUrl}/historique`);
  }

  update(rate: number): Observable<CommissionRateResponse> {
    return this.http.post<CommissionRateResponse>(this.baseUrl, { rate });
  }
}
