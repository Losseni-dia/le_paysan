package shop.le_paysan.backend.shop.dto;

import java.math.BigDecimal;

import shop.le_paysan.backend.shop.ShopDeliveryTier;

public record ShopDeliveryTierResponse(Long id, Integer maxDistanceKm, BigDecimal price) {

    public static ShopDeliveryTierResponse from(ShopDeliveryTier tier) {
        return new ShopDeliveryTierResponse(tier.getId(), tier.getMaxDistanceKm(), tier.getPrice());
    }
}
