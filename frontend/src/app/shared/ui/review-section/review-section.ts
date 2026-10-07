import { Component, inject, input, OnInit, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { AuthService } from '../../../core/auth/auth.service';
import { PageResponse } from '../../../core/shop/shop.model';
import { ReviewService } from '../../../core/review/review.service';
import { ProductReviewResponse, RatingSummaryResponse, ShopReviewResponse } from '../../../core/review/review.model';
import { StarRating } from '../star-rating/star-rating';

type ReviewEntity = 'product' | 'shop';
type ReviewItem = ProductReviewResponse | ShopReviewResponse;

@Component({
  selector: 'app-review-section',
  imports: [StarRating, DatePipe, DecimalPipe],
  templateUrl: './review-section.html',
  styleUrl: './review-section.scss',
})
export class ReviewSection implements OnInit {
  private readonly reviewService = inject(ReviewService);
  readonly authService = inject(AuthService);

  readonly entityType = input.required<ReviewEntity>();
  readonly entityId = input.required<number>();
  /** Vrai si l'utilisateur connecté est le propriétaire (ne peut pas noter). */
  readonly isOwner = input(false);

  readonly summary = signal<RatingSummaryResponse | null>(null);
  readonly reviews = signal<ReviewItem[]>([]);
  readonly myReview = signal<ReviewItem | null>(null);
  readonly loading = signal(true);

  readonly editing = signal(false);
  readonly formRating = signal(0);
  readonly formComment = signal('');
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.load();
  }

  startEdit(): void {
    const mine = this.myReview();
    this.formRating.set(mine?.rating ?? 0);
    this.formComment.set(mine?.comment ?? '');
    this.editing.set(true);
    this.error.set(null);
  }

  cancelEdit(): void {
    this.editing.set(false);
  }

  submit(): void {
    if (this.formRating() < 1) {
      this.error.set('Choisissez une note.');
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    const request = { rating: this.formRating(), comment: this.formComment() || undefined };
    const obs: Observable<ReviewItem> =
      this.entityType() === 'product'
        ? this.reviewService.submitProductReview(this.entityId(), request)
        : this.reviewService.submitShopReview(this.entityId(), request);
    obs.subscribe({
      next: () => {
        this.submitting.set(false);
        this.editing.set(false);
        this.load();
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.error.set(err.error?.message ?? "Impossible d'enregistrer votre avis.");
      },
    });
  }

  private load(): void {
    this.loading.set(true);
    const isProduct = this.entityType() === 'product';
    const summary$ = isProduct
      ? this.reviewService.getProductSummary(this.entityId())
      : this.reviewService.getShopSummary(this.entityId());
    const list$: Observable<PageResponse<ReviewItem>> = isProduct
      ? this.reviewService.listForProduct(this.entityId())
      : this.reviewService.listForShop(this.entityId());

    summary$.subscribe({ next: (s) => this.summary.set(s) });
    list$.subscribe({
      next: (page) => {
        this.reviews.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });

    if (this.authService.isAuthenticated() && !this.isOwner()) {
      const mine$: Observable<ReviewItem | null> = isProduct
        ? this.reviewService.getMyProductReview(this.entityId())
        : this.reviewService.getMyShopReview(this.entityId());
      mine$.subscribe({ next: (r) => this.myReview.set(r), error: () => this.myReview.set(null) });
    }
  }
}
