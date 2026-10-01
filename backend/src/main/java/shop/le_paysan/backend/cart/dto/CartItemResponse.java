package shop.le_paysan.backend.cart.dto;

import java.math.BigDecimal;
import java.util.List;

import shop.le_paysan.backend.cart.CartItem;

public record CartItemResponse(
        Long productId,
        String productName,
        String imageUrl,
        Long shopId,
        String shopName,
        BigDecimal unitPrice,
        Integer quantity,
        String deliveryMode,
        BigDecimal deliveryPrice,
        String deliveryAddress,
        BigDecimal lineSubtotal,
        List<String> availableDeliveryModes,
        Integer stockAvailable,
        boolean available) {

    public static CartItemResponse from(CartItem item) {
        var product = item.getProduct();
        var shop = product.getShop();

        var matchingOption = product.getDeliveryOptions().stream()
                .filter(o -> o.getMode().equals(item.getDeliveryMode()))
                .findFirst();
        BigDecimal deliveryPrice = matchingOption.map(o -> o.getDeliveryPrice()).orElse(BigDecimal.ZERO);

        boolean isPublished = shop.getStatus().equals("VALIDEE") && product.getStatus().equals("VALIDE");
        boolean inStock = product.getStockQuantity() >= item.getQuantity();
        boolean modeStillOffered = matchingOption.isPresent();

        BigDecimal lineSubtotal = product.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                product.getId(),
                product.getName(),
                product.getImages().isEmpty() ? null : product.getImages().get(0).getUrl(),
                shop.getId(),
                shop.getName(),
                product.getUnitPrice(),
                item.getQuantity(),
                item.getDeliveryMode(),
                deliveryPrice,
                item.getDeliveryAddress(),
                lineSubtotal,
                product.getDeliveryOptions().stream().map(o -> o.getMode()).toList(),
                product.getStockQuantity(),
                isPublished && inStock && modeStillOffered);
    }
}
