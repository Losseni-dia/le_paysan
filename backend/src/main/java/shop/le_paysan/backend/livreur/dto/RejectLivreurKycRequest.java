package shop.le_paysan.backend.livreur.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectLivreurKycRequest(@NotBlank String reason) {
}
