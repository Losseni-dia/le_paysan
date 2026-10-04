import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AccountMenu } from '../../shared/ui/account-menu/account-menu';
import { NotificationBell } from '../../shared/ui/notification-bell/notification-bell';

@Component({
  selector: 'app-livreur-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NotificationBell, AccountMenu],
  templateUrl: './livreur-layout.html',
  styleUrl: './livreur-layout.scss',
})
export class LivreurLayout {
  readonly collapsed = signal(false);

  toggleSidebar(): void {
    this.collapsed.update((v) => !v);
  }
}
