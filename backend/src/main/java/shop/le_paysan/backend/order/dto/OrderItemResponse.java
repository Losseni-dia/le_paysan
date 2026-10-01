package shop.le_paysan.backend.order.dto;

import java.math.BigDecimal;

import shop.le_paysan.backend.order.OrderItem;

public record OrderItemResponse(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        String deliveryMode,
        BigDecimal deliveryPrice,
        String deliveryAddress,
        BigDecimal lineSubtotal) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProductNameSnapshot(),
                item.getUnitPriceSnapshot(),
                item.getQuantity(),
                item.getDeliveryMode(),
                item.getDeliveryPrice(),
                item.getDeliveryAddress(),
                item.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())));
    }
}
