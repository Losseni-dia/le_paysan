import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import { environment } from '../../../environments/environment';

/**
 * Les documents uploades (KYC, selfies...) sont servis par un endpoint protege
 * (JWT), donc impossible a referencer directement dans un <img src="...">.
 * Ce service recupere le fichier en blob (l'intercepteur HTTP attache le
 * token) et fournit une object URL utilisable dans le template.
 */
@Injectable({
  providedIn: 'root',
})
export class FileUrlService {
  private readonly http = inject(HttpClient);

  getBlobUrl(relativePath: string): Observable<string> {
    return this.http
      .get(`${environment.apiBaseUrl}/files/${relativePath}`, { responseType: 'blob' })
      .pipe(map((blob) => URL.createObjectURL(blob)));
  }
}
