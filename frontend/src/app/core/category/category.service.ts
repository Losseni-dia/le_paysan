import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CategoryResponse } from './category.model';

@Injectable({
  providedIn: 'root',
})
export class CategoryService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/categories`;
  private readonly adminUrl = `${environment.apiBaseUrl}/admin/categories`;

  list(): Observable<CategoryResponse[]> {
    return this.http.get<CategoryResponse[]>(this.baseUrl);
  }

  create(name: string): Observable<CategoryResponse> {
    return this.http.post<CategoryResponse>(this.baseUrl, { name });
  }

  /** Suggestion de nouvelle catégorie par un vendeur (statut EN_ATTENTE jusqu'à validation admin). */
  suggest(name: string): Observable<CategoryResponse> {
    return this.http.post<CategoryResponse>(`${this.baseUrl}/suggestions`, { name });
  }

  // --- Admin ---

  listForAdmin(): Observable<CategoryResponse[]> {
    return this.http.get<CategoryResponse[]>(this.adminUrl);
  }

  validate(id: number): Observable<CategoryResponse> {
    return this.http.post<CategoryResponse>(`${this.adminUrl}/${id}/validate`, {});
  }

  update(id: number, name: string): Observable<CategoryResponse> {
    return this.http.patch<CategoryResponse>(`${this.adminUrl}/${id}`, { name });
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.adminUrl}/${id}`);
  }
}
