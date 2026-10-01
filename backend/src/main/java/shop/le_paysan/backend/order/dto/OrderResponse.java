package shop.le_paysan.backend.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import shop.le_paysan.backend.order.Order;

public record OrderResponse(
        Long id,
        String orderNumber,
        Long shopId,
        String shopName,
        String status,
        BigDecimal subtotalAmount,
        BigDecimal deliveryAmount,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        LocalDateTime createdAt) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getShop().getId(),
                order.getShop().getName(),
                order.getStatus(),
                order.getSubtotalAmount(),
                order.getDeliveryAmount(),
                order.getTotalAmount(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getCreatedAt());
    }
}
