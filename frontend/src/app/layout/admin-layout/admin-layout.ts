import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

import { LanguageSwitcher } from '../../shared/ui/language-switcher/language-switcher';
import { NotificationBell } from '../../shared/ui/notification-bell/notification-bell';

@Component({
  selector: 'app-admin-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, TranslatePipe, LanguageSwitcher, NotificationBell],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.scss',
})
export class AdminLayout {
  readonly collapsed = signal(false);

  toggleSidebar(): void {
    this.collapsed.update((v) => !v);
  }
}
