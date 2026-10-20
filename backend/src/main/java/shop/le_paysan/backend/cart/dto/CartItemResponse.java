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
        String deliveryCityName,
        BigDecimal deliveryCityLat,
        BigDecimal deliveryCityLng,
        BigDecimal lineSubtotal,
        List<String> availableDeliveryModes,
        Integer stockAvailable,
        boolean available,
        boolean deliveryOutOfRange) {

    public static CartItemResponse from(CartItem item) {
        var matchingOption = item.getProduct().getDeliveryOptions().stream()
                .filter(o -> o.getMode().equals(item.getDeliveryMode()))
                .findFirst();
        BigDecimal deliveryPrice = matchingOption.map(o -> o.getDeliveryPrice()).orElse(BigDecimal.ZERO);
        return build(item, deliveryPrice, false);
    }

    public static CartItemResponse from(CartItem item, BigDecimal deliveryPrice) {
        return build(item, deliveryPrice, false);
    }

    /** La commande n'est pas livrable a la ville choisie par l'acheteur (hors de tous les paliers de la boutique). */
    public static CartItemResponse outOfRange(CartItem item) {
        return build(item, BigDecimal.ZERO, true);
    }

    private static CartItemResponse build(CartItem item, BigDecimal deliveryPrice, boolean deliveryOutOfRange) {
        var product = item.getProduct();
        var shop = product.getShop();

        var matchingOption = product.getDeliveryOptions().stream()
                .filter(o -> o.getMode().equals(item.getDeliveryMode()))
                .findFirst();

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
                item.getDeliveryCityName(),
                item.getDeliveryCityLat(),
                item.getDeliveryCityLng(),
                lineSubtotal,
                product.getDeliveryOptions().stream().map(o -> o.getMode()).toList(),
                product.getStockQuantity(),
                isPublished && inStock && modeStillOffered && !deliveryOutOfRange,
                deliveryOutOfRange);
    }
}
