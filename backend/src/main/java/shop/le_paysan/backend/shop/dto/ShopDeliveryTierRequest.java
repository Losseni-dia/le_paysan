package shop.le_paysan.backend.shop.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ShopDeliveryTierRequest(@NotNull @Min(1) Integer maxDistanceKm, @NotNull @DecimalMin("0") BigDecimal price) {
}
