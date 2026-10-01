package shop.le_paysan.backend.shop.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateShopRequest(
        String description,
        String category,
        @NotBlank String addressCity,
        @NotBlank String addressDetail,
        @NotNull BigDecimal latitude,
        @NotNull BigDecimal longitude,
        @NotBlank String phone) {
}
