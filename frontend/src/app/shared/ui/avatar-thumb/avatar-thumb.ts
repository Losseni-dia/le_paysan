import { Component, Input, OnChanges, inject, signal } from '@angular/core';

import { FileUrlService } from '../../../core/common/file-url.service';

/** Petite photo de profil ronde, chargee depuis un endpoint protege (JWT). */
@Component({
  selector: 'app-avatar-thumb',
  imports: [],
  templateUrl: './avatar-thumb.html',
  styleUrl: './avatar-thumb.scss',
})
export class AvatarThumb implements OnChanges {
  private readonly fileUrlService = inject(FileUrlService);

  @Input({ required: true }) path!: string | null;
  @Input() alt = 'Photo';

  readonly url = signal<string | null>(null);

  ngOnChanges(): void {
    if (!this.path) {
      this.url.set(null);
      return;
    }
    this.fileUrlService.getBlobUrl(this.path).subscribe({
      next: (url) => this.url.set(url),
      error: () => this.url.set(null),
    });
  }
}
