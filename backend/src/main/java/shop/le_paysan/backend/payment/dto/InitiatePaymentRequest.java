package shop.le_paysan.backend.payment.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record InitiatePaymentRequest(@NotEmpty List<Long> orderIds) {
}
