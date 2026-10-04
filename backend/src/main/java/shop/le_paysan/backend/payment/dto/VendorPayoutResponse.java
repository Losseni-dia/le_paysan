package shop.le_paysan.backend.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import shop.le_paysan.backend.payment.VendorPayout;

public record VendorPayoutResponse(
        Long id,
        String orderNumber,
        Long shopId,
        String shopName,
        BigDecimal amount,
        String status,
        String fedapayPayoutId,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime processedAt) {

    public static VendorPayoutResponse from(VendorPayout p) {
        return new VendorPayoutResponse(
                p.getId(),
                p.getOrder().getOrderNumber(),
                p.getShop().getId(),
                p.getShop().getName(),
                p.getAmount(),
                p.getStatus(),
                p.getFedapayPayoutId(),
                p.getFailureReason(),
                p.getCreatedAt(),
                p.getProcessedAt());
    }
}
