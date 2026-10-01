package shop.le_paysan.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank String firstName, @NotBlank String lastName, @Email String email, String phone) {
}
