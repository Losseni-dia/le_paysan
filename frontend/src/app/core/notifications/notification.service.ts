import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { AppNotification } from './notification.model';

/**
 * Centre de notifications in-app — canal obligatoire (cf. docs/02 UC-13),
 * indépendant du fait que l'utilisateur ait renseigné un email ou non.
 */
@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private readonly http = inject(HttpClient);

  readonly notifications = signal<AppNotification[]>([]);
  readonly unreadCount = signal(0);

  fetchUnreadCount(): Observable<{ count: number }> {
    return this.http
      .get<{ count: number }>('/api/notifications/unread-count')
      .pipe(tap((res) => this.unreadCount.set(res.count)));
  }

  fetchNotifications(unreadOnly = false): Observable<AppNotification[]> {
    return this.http
      .get<AppNotification[]>('/api/notifications', { params: { unreadOnly } })
      .pipe(tap((list) => this.notifications.set(list)));
  }

  markAsRead(id: number): Observable<void> {
    return this.http.patch<void>(`/api/notifications/${id}/read`, {});
  }

  markAllAsRead(): Observable<void> {
    return this.http.patch<void>('/api/notifications/read-all', {});
  }
}
