import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Observable } from 'rxjs';

import { PageResponse } from '../../../core/shop/shop.model';
import { ReviewService } from '../../../core/review/review.service';
import { ProductReviewResponse, ShopReviewResponse } from '../../../core/review/review.model';
import { StarRating } from '../../../shared/ui/star-rating/star-rating';

type Tab = 'produits' | 'boutiques';
type ReviewItem = ProductReviewResponse | ShopReviewResponse;

@Component({
  selector: 'app-admin-reviews',
  imports: [DatePipe, StarRating],
  templateUrl: './reviews.html',
  styleUrl: './reviews.scss',
})
export class Reviews {
  private readonly reviewService = inject(ReviewService);

  readonly tab = signal<Tab>('produits');
  readonly statusFilter = signal<string>('');
  readonly reviews = signal<ReviewItem[]>([]);
  readonly loading = signal(true);

  constructor() {
    this.load();
  }

  isProductReview(r: ReviewItem): r is ProductReviewResponse {
    return 'productId' in r;
  }

  selectTab(tab: Tab): void {
    this.tab.set(tab);
    this.load();
  }

  selectStatus(status: string): void {
    this.statusFilter.set(status);
    this.load();
  }

  moderate(review: ReviewItem, status: string): void {
    const reason = status === 'MASQUE' ? (prompt('Motif (optionnel)') ?? undefined) : undefined;
    const obs: Observable<ReviewItem> =
      this.tab() === 'produits'
        ? this.reviewService.moderateProductReview(review.id, status, reason)
        : this.reviewService.moderateShopReview(review.id, status, reason);
    obs.subscribe({ next: () => this.load() });
  }

  private load(): void {
    this.loading.set(true);
    const status = this.statusFilter() || undefined;
    const obs: Observable<PageResponse<ReviewItem>> =
      this.tab() === 'produits'
        ? this.reviewService.listProductReviewsForAdmin(status)
        : this.reviewService.listShopReviewsForAdmin(status);
    obs.subscribe({
      next: (page) => {
        this.reviews.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
