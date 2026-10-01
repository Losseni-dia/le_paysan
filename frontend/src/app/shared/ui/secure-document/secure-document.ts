import { Component, Input, OnChanges, inject, signal } from '@angular/core';

import { FileUrlService } from '../../../core/common/file-url.service';

/** Affiche un document uploade (KYC...) servi par un endpoint protege (JWT). */
@Component({
  selector: 'app-secure-document',
  imports: [],
  templateUrl: './secure-document.html',
  styleUrl: './secure-document.scss',
})
export class SecureDocument implements OnChanges {
  private readonly fileUrlService = inject(FileUrlService);

  @Input({ required: true }) path!: string | null;
  @Input() label = 'Document';

  readonly url = signal<string | null>(null);
  readonly loading = signal(false);

  ngOnChanges(): void {
    if (!this.path) {
      this.url.set(null);
      return;
    }
    this.loading.set(true);
    this.fileUrlService.getBlobUrl(this.path).subscribe({
      next: (url) => {
        this.url.set(url);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
