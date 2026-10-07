export type ReviewStatus = 'VISIBLE' | 'MASQUE';

export interface RatingSummaryResponse {
  average: number;
  count: number;
}

export interface ProductReviewResponse {
  id: number;
  productId: number;
  productName: string;
  buyerUserId: number;
  buyerFullName: string;
  rating: number;
  comment: string | null;
  status: ReviewStatus;
  moderationReason: string | null;
  createdAt: string;
}

export interface ShopReviewResponse {
  id: number;
  shopId: number;
  shopName: string;
  buyerUserId: number;
  buyerFullName: string;
  rating: number;
  comment: string | null;
  status: ReviewStatus;
  moderationReason: string | null;
  createdAt: string;
}

export interface SubmitReviewRequest {
  rating: number;
  comment?: string;
}
