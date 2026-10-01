import { Component, inject } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-language-switcher',
  imports: [],
  templateUrl: './language-switcher.html',
  styleUrl: './language-switcher.scss',
})
export class LanguageSwitcher {
  private readonly translate = inject(TranslateService);

  readonly languages = ['fr', 'en'];

  get currentLang(): string {
    return this.translate.currentLang() ?? 'fr';
  }

  use(lang: string): void {
    this.translate.use(lang);
  }
}
