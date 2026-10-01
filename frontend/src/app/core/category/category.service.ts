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

  list(): Observable<CategoryResponse[]> {
    return this.http.get<CategoryResponse[]>(this.baseUrl);
  }

  create(name: string): Observable<CategoryResponse> {
    return this.http.post<CategoryResponse>(this.baseUrl, { name });
  }
}
