package shop.le_paysan.backend.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PayoutAccountRequest(
        @NotBlank @Pattern(regexp = "ORANGE|MTN|MOOV|WAVE") String operator,
        @NotBlank String phoneNumber,
        @NotBlank String accountHolderName) {
}
