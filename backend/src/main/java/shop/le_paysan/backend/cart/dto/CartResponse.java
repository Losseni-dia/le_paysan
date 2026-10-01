package shop.le_paysan.backend.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        List<CartItemResponse> items, BigDecimal subtotalAmount, BigDecimal deliveryAmount, BigDecimal totalAmount) {

    public static CartResponse from(List<CartItemResponse> items) {
        BigDecimal subtotal = items.stream().map(CartItemResponse::lineSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal delivery = items.stream()
                .filter(i -> "LIVRAISON_DOMICILE".equals(i.deliveryMode()))
                .map(CartItemResponse::deliveryPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(items, subtotal, delivery, subtotal.add(delivery));
    }
}
