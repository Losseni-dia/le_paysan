import { Component, inject, signal } from '@angular/core';

import { NotificationService } from '../../../core/notifications/notification.service';

@Component({
  selector: 'app-notification-bell',
  imports: [],
  templateUrl: './notification-bell.html',
  styleUrl: './notification-bell.scss',
})
export class NotificationBell {
  private readonly notificationService = inject(NotificationService);

  readonly open = signal(false);
  readonly unreadCount = this.notificationService.unreadCount;
  readonly notifications = this.notificationService.notifications;

  toggle(): void {
    this.open.update((v) => !v);
    if (this.open()) {
      this.notificationService.fetchNotifications().subscribe();
    }
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe(() => this.unreadCount.set(0));
  }
}
