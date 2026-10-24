package shop.le_paysan.backend.delivery.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import shop.le_paysan.backend.delivery.DeliveryRequest;
import shop.le_paysan.backend.order.Order;

/** Vue cote livreur d'une demande de livraison : infos de recuperation et de remise. */
public record DeliveryRequestResponse(
        Long id,
        Long orderId,
        String orderNumber,
        String status,
        String shopName,
        String pickupCity,
        String pickupAddress,
        String buyerFullName,
        String deliveryAddress,
        BigDecimal totalAmount,
        LocalDateTime createdAt) {

    public static DeliveryRequestResponse from(DeliveryRequest request) {
        Order order = request.getOrder();
        String deliveryAddress = order.getItems().stream()
                .map(i -> i.getDeliveryAddress())
                .filter(a -> a != null && !a.isBlank())
                .findFirst()
                .orElse(null);
        return new DeliveryRequestResponse(
                request.getId(),
                order.getId(),
                order.getOrderNumber(),
                request.getStatus(),
                order.getShop().getName(),
                order.getShop().getAddressCity(),
                order.getShop().getAddressDetail(),
                order.getBuyerUser().getFirstName() + " " + order.getBuyerUser().getLastName(),
                deliveryAddress,
                order.getTotalAmount(),
                request.getCreatedAt());
    }
}
