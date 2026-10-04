package shop.le_paysan.backend.commission.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import shop.le_paysan.backend.commission.CommissionSettings;

public record CommissionRateResponse(Long id, BigDecimal rate, boolean active, LocalDateTime createdAt) {

    public static CommissionRateResponse from(CommissionSettings settings) {
        return new CommissionRateResponse(
                settings.getId(), settings.getRate(), settings.isActive(), settings.getCreatedAt());
    }
}
