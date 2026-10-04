package shop.le_paysan.backend.commission.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record UpdateCommissionRequest(
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal rate) {
}
