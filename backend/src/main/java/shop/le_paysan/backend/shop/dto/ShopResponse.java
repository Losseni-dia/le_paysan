package shop.le_paysan.backend.shop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import shop.le_paysan.backend.shop.Shop;

public record ShopResponse(
        Long id,
        String ownerType,
        String name,
        String description,
        String category,
        String addressCity,
        String addressDetail,
        BigDecimal latitude,
        BigDecimal longitude,
        String phone,
        String logoUrl,
        String coverUrl,
        String status,
        LocalDateTime createdAt) {

    public static ShopResponse from(Shop shop) {
        return new ShopResponse(
                shop.getId(),
                shop.getOwnerType(),
                shop.getName(),
                shop.getDescription(),
                shop.getCategory(),
                shop.getAddressCity(),
                shop.getAddressDetail(),
                shop.getLatitude(),
                shop.getLongitude(),
                shop.getPhone(),
                shop.getLogoUrl(),
                shop.getCoverUrl(),
                shop.getStatus(),
                shop.getCreatedAt());
    }
}
