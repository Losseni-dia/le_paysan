package shop.le_paysan.backend.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import shop.le_paysan.backend.order.Order;

public record AdminOrderResponse(
        Long id,
        String orderNumber,
        Long shopId,
        String shopName,
        Long buyerUserId,
        String buyerUsername,
        String buyerFullName,
        String status,
        BigDecimal subtotalAmount,
        BigDecimal deliveryAmount,
        BigDecimal commissionAmount,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        List<OrderStatusHistoryResponse> historique,
        Long assignedLivreurId,
        String assignedLivreurName,
        LocalDateTime createdAt) {

    public static AdminOrderResponse from(Order order) {
        return from(order, List.of());
    }

    public static AdminOrderResponse from(Order order, List<OrderStatusHistoryResponse> historique) {
        return new AdminOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getShop().getId(),
                order.getShop().getName(),
                order.getBuyerUser().getId(),
                order.getBuyerUser().getUsername(),
                order.getBuyerUser().getFirstName() + " " + order.getBuyerUser().getLastName(),
                order.getStatus(),
                order.getSubtotalAmount(),
                order.getDeliveryAmount(),
                order.getCommissionAmount(),
                order.getTotalAmount(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                historique,
                order.getAssignedLivreur() != null ? order.getAssignedLivreur().getId() : null,
                order.getAssignedLivreur() != null
                        ? order.getAssignedLivreur().getFirstName() + " " + order.getAssignedLivreur().getLastName()
                        : null,
                order.getCreatedAt());
    }
}
