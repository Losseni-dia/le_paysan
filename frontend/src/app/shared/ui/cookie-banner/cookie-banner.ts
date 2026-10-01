import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

const COOKIE_CONSENT_KEY = 'le_paysan_cookie_consent';

/**
 * Bandeau affiche des la premiere visite (cf. docs/08-mentions-legales-cgu-rgpd.md §8).
 * Au MVP, seuls des cookies techniques (JWT) existent : bandeau informatif,
 * pas strictement bloquant.
 */
@Component({
  selector: 'app-cookie-banner',
  imports: [RouterLink],
  templateUrl: './cookie-banner.html',
  styleUrl: './cookie-banner.scss',
})
export class CookieBanner {
  readonly visible = signal(localStorage.getItem(COOKIE_CONSENT_KEY) === null);

  accept(): void {
    localStorage.setItem(COOKIE_CONSENT_KEY, 'accepted');
    this.visible.set(false);
  }

  decline(): void {
    localStorage.setItem(COOKIE_CONSENT_KEY, 'declined');
    this.visible.set(false);
  }
}
