import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

export interface InitiatePaymentResponse {
  paymentUrl: string;
}

@Injectable({
  providedIn: 'root',
})
export class PaymentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/payments`;

  initiate(orderIds: number[]): Observable<InitiatePaymentResponse> {
    return this.http.post<InitiatePaymentResponse>(this.baseUrl, { orderIds });
  }
}
