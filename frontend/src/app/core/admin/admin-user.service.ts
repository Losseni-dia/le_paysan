import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PageResponse } from '../shop/shop.model';
import { AdminUserResponse } from './admin-user.model';

@Injectable({
  providedIn: 'root',
})
export class AdminUserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin/users`;

  list(search?: string, role?: string, page = 0): Observable<PageResponse<AdminUserResponse>> {
    let params = new HttpParams().set('page', page);
    if (search) params = params.set('search', search);
    if (role) params = params.set('role', role);
    return this.http.get<PageResponse<AdminUserResponse>>(this.baseUrl, { params });
  }

  setEnabled(id: number, enabled: boolean): Observable<AdminUserResponse> {
    return this.http.patch<AdminUserResponse>(`${this.baseUrl}/${id}/enabled`, { enabled });
  }

  setRoles(id: number, roles: string[]): Observable<AdminUserResponse> {
    return this.http.patch<AdminUserResponse>(`${this.baseUrl}/${id}/roles`, { roles });
  }
}
