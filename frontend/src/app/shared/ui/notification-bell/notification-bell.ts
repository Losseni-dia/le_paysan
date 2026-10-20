import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { interval } from 'rxjs';

import { NotificationService } from '../../../core/notifications/notification.service';
import { AppNotification } from '../../../core/notifications/notification.model';

const POLL_INTERVAL_MS = 30000;

@Component({
  selector: 'app-notification-bell',
  imports: [RouterLink],
  templateUrl: './notification-bell.html',
  styleUrl: './notification-bell.scss',
})
export class NotificationBell {
  private readonly notificationService = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);

  readonly open = signal(false);
  readonly unreadCount = this.notificationService.unreadCount;
  readonly notifications = this.notificationService.notifications;

  constructor() {
    this.notificationService.fetchUnreadCount().subscribe();
    const sub = interval(POLL_INTERVAL_MS).subscribe(() => this.notificationService.fetchUnreadCount().subscribe());
    this.destroyRef.onDestroy(() => sub.unsubscribe());
  }

  toggle(): void {
    this.open.update((v) => !v);
    if (this.open()) {
      this.notificationService.fetchNotifications().subscribe();
      if (this.unreadCount() > 0) {
        this.notificationService.markAllAsRead().subscribe(() => this.unreadCount.set(0));
      }
    }
  }

  openNotification(): void {
    this.open.set(false);
  }
}
