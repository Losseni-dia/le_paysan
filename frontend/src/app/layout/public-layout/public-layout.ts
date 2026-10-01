import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

import { AuthService } from '../../core/auth/auth.service';
import { CartService } from '../../core/cart/cart.service';
import { AccountMenu } from '../../shared/ui/account-menu/account-menu';
import { LanguageSwitcher } from '../../shared/ui/language-switcher/language-switcher';
import { NotificationBell } from '../../shared/ui/notification-bell/notification-bell';

@Component({
  selector: 'app-public-layout',
  imports: [RouterOutlet, RouterLink, TranslatePipe, LanguageSwitcher, NotificationBell, AccountMenu],
  templateUrl: './public-layout.html',
  styleUrl: './public-layout.scss',
})
export class PublicLayout {
  readonly authService = inject(AuthService);
  readonly cartService = inject(CartService);

  constructor() {
    if (this.authService.isAuthenticated()) {
      this.cartService.fetch().subscribe({ error: () => void 0 });
    }
  }
}
