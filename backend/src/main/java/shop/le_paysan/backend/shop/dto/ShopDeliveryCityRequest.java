package shop.le_paysan.backend.shop.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShopDeliveryCityRequest(@NotBlank String cityName, @NotNull @DecimalMin("0") BigDecimal price) {
}
