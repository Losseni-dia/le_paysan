import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

import { AuthService } from '../../../core/auth/auth.service';
import { LanguageSwitcher } from '../language-switcher/language-switcher';

/**
 * Avatar (initiale) + menu deroulant "Mon espace", affiche dans les trois
 * layouts (public, vendeur, admin) pour un acces constant a la navigation
 * transversale (boutique/admin, langue, deconnexion) peu importe l'endroit
 * ou se trouve l'utilisateur.
 */
@Component({
  selector: 'app-account-menu',
  imports: [RouterLink, TranslatePipe, LanguageSwitcher],
  templateUrl: './account-menu.html',
  styleUrl: './account-menu.scss',
})
export class AccountMenu {
  readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly open = signal(false);

  get userInitial(): string {
    const user = this.authService.currentUser();
    const source = user?.firstName || user?.username || '?';
    return source.charAt(0).toUpperCase();
  }

  toggle(): void {
    this.open.update((v) => !v);
  }

  close(): void {
    this.open.set(false);
  }

  logout(): void {
    this.close();
    this.authService.logout();
    this.router.navigateByUrl('/');
  }
}
