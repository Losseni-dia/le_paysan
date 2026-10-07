package shop.le_paysan.backend.review.dto;

import java.time.LocalDateTime;

import shop.le_paysan.backend.review.ShopReview;

public record ShopReviewResponse(
        Long id,
        Long shopId,
        String shopName,
        Long buyerUserId,
        String buyerFullName,
        Integer rating,
        String comment,
        String status,
        String moderationReason,
        LocalDateTime createdAt) {

    public static ShopReviewResponse from(ShopReview r) {
        return new ShopReviewResponse(
                r.getId(),
                r.getShop().getId(),
                r.getShop().getName(),
                r.getBuyerUser().getId(),
                r.getBuyerUser().getFirstName() + " " + r.getBuyerUser().getLastName(),
                r.getRating(),
                r.getComment(),
                r.getStatus(),
                r.getModerationReason(),
                r.getCreatedAt());
    }
}
