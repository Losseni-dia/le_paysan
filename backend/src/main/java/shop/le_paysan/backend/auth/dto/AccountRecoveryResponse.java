package shop.le_paysan.backend.auth.dto;

import java.time.LocalDateTime;

import shop.le_paysan.backend.auth.AccountRecoveryRequest;

public record AccountRecoveryResponse(
        Long id,
        Long userId,
        String username,
        String submittedSelfieUrl,
        String referenceSelfieUrl,
        String status,
        String reviewReason,
        LocalDateTime createdAt) {

    public static AccountRecoveryResponse from(AccountRecoveryRequest entity) {
        return new AccountRecoveryResponse(
                entity.getId(),
                entity.getUser().getId(),
                entity.getUser().getUsername(),
                entity.getSubmittedSelfieUrl(),
                entity.getUser().getSecuritySelfieUrl(),
                entity.getStatus(),
                entity.getReviewReason(),
                entity.getCreatedAt());
    }
}
