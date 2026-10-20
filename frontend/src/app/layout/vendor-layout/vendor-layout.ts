import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

import { AccountMenu } from '../../shared/ui/account-menu/account-menu';
import { NotificationBell } from '../../shared/ui/notification-bell/notification-bell';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-vendor-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, TranslatePipe, NotificationBell, AccountMenu],
  templateUrl: './vendor-layout.html',
  styleUrl: './vendor-layout.scss',
})
export class VendorLayout {
  readonly authService = inject(AuthService);
  readonly collapsed = signal(false);

  toggleSidebar(): void {
    this.collapsed.update((v) => !v);
  }
}
