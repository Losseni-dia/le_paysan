import { Component, Input } from '@angular/core';

import { LEGAL_CONTENT, LegalPageContent } from '../legal-content';

@Component({
  selector: 'app-legal-page',
  imports: [],
  templateUrl: './legal-page.html',
  styleUrl: './legal-page.scss',
})
export class LegalPage {
  content: LegalPageContent | null = null;

  /** Alimenté par le binding d'input de route (withComponentInputBinding), cf. app.routes.ts. */
  @Input() set slug(value: string) {
    this.content = LEGAL_CONTENT[value] ?? null;
  }
}
