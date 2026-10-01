package shop.le_paysan.backend.product.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectProductRequest(@NotBlank String reason) {
}
