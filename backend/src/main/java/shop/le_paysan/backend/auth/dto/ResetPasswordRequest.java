package shop.le_paysan.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caracteres")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Za-z]).+$", message = "Le mot de passe doit contenir au moins une lettre et un chiffre")
        String newPassword) {
}
