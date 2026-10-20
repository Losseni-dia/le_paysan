import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

export interface ContactMessageRequest {
  fullName: string;
  email: string;
  subject: string;
  message: string;
}

@Injectable({
  providedIn: 'root',
})
export class ContactService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/contact`;

  submit(request: ContactMessageRequest): Observable<void> {
    return this.http.post<void>(this.baseUrl, request);
  }
}
