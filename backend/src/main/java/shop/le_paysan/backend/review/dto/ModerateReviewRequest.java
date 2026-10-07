package shop.le_paysan.backend.review.dto;

import jakarta.validation.constraints.NotBlank;

public record ModerateReviewRequest(@NotBlank String status, String reason) {
}
