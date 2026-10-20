import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AccountMenu } from '../../shared/ui/account-menu/account-menu';
import { NotificationBell } from '../../shared/ui/notification-bell/notification-bell';
import { LivreurService } from '../../core/livreur/livreur.service';

@Component({
  selector: 'app-livreur-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NotificationBell, AccountMenu],
  templateUrl: './livreur-layout.html',
  styleUrl: './livreur-layout.scss',
})
export class LivreurLayout {
  private readonly livreurService = inject(LivreurService);

  readonly collapsed = signal(false);
  readonly available = signal<boolean | null>(null);
  readonly updatingAvailability = signal(false);
  readonly availabilityConfirmed = signal(false);
  private confirmTimeout?: ReturnType<typeof setTimeout>;

  constructor() {
    this.livreurService.getMe().subscribe({ next: (me) => this.available.set(me.available) });
  }

  toggleSidebar(): void {
    this.collapsed.update((v) => !v);
  }

  toggleAvailability(): void {
    const next = !this.available();
    this.updatingAvailability.set(true);
    this.livreurService.setAvailability(next).subscribe({
      next: (updated) => {
        this.available.set(updated.available);
        this.updatingAvailability.set(false);
        this.availabilityConfirmed.set(true);
        clearTimeout(this.confirmTimeout);
        this.confirmTimeout = setTimeout(() => this.availabilityConfirmed.set(false), 2500);
      },
      error: () => this.updatingAvailability.set(false),
    });
  }
}
