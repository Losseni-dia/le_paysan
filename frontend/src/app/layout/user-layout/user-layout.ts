import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

import { AuthService } from '../../core/auth/auth.service';
import { AccountMenu } from '../../shared/ui/account-menu/account-menu';
import { NotificationBell } from '../../shared/ui/notification-bell/notification-bell';

@Component({
  selector: 'app-user-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, TranslatePipe, NotificationBell, AccountMenu],
  templateUrl: './user-layout.html',
  styleUrl: './user-layout.scss',
})
export class UserLayout {
  readonly authService = inject(AuthService);
  readonly collapsed = signal(false);

  toggleSidebar(): void {
    this.collapsed.update((v) => !v);
  }
}
