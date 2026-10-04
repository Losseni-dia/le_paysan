package shop.le_paysan.backend.order.dto;

import java.time.LocalDateTime;

import shop.le_paysan.backend.order.OrderStatusHistory;

public record OrderStatusHistoryResponse(
        String status, String changedBy, String note, String proofType, String proofUrl, LocalDateTime changedAt) {

    public static OrderStatusHistoryResponse from(OrderStatusHistory history) {
        return new OrderStatusHistoryResponse(
                history.getStatus(),
                history.getChangedBy(),
                history.getNote(),
                history.getProofType(),
                history.getProofUrl(),
                history.getChangedAt());
    }
}
