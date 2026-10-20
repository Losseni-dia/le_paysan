package shop.le_paysan.backend.livreur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetLivreurPasswordRequest(@NotBlank @Size(min = 8) String password) {
}
