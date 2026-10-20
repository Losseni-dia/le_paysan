package shop.le_paysan.backend.order.dto;

public record BulkAssignLivreurResult(Long orderId, String orderNumber, boolean success, String message) {
}
