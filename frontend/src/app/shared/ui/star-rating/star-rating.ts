import { Component, computed, input, output, signal } from '@angular/core';

@Component({
  selector: 'app-star-rating',
  imports: [],
  templateUrl: './star-rating.html',
  styleUrl: './star-rating.scss',
})
export class StarRating {
  readonly rating = input<number>(0);
  readonly interactive = input(false);
  readonly ratingChange = output<number>();

  readonly stars = [1, 2, 3, 4, 5];
  readonly hovered = signal<number | null>(null);

  readonly displayRating = computed(() => this.hovered() ?? this.rating());

  onClick(value: number): void {
    if (this.interactive()) {
      this.ratingChange.emit(value);
    }
  }

  onHover(value: number): void {
    if (this.interactive()) {
      this.hovered.set(value);
    }
  }

  onLeave(): void {
    this.hovered.set(null);
  }
}
