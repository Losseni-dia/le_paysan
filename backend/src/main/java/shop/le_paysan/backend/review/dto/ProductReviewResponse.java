package shop.le_paysan.backend.review.dto;

import java.time.LocalDateTime;

import shop.le_paysan.backend.review.ProductReview;

public record ProductReviewResponse(
        Long id,
        Long productId,
        String productName,
        Long buyerUserId,
        String buyerFullName,
        Integer rating,
        String comment,
        String status,
        String moderationReason,
        LocalDateTime createdAt) {

    public static ProductReviewResponse from(ProductReview r) {
        return new ProductReviewResponse(
                r.getId(),
                r.getProduct().getId(),
                r.getProduct().getName(),
                r.getBuyerUser().getId(),
                r.getBuyerUser().getFirstName() + " " + r.getBuyerUser().getLastName(),
                r.getRating(),
                r.getComment(),
                r.getStatus(),
                r.getModerationReason(),
                r.getCreatedAt());
    }
}
