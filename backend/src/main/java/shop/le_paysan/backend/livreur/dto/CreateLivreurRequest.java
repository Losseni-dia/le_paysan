package shop.le_paysan.backend.livreur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLivreurRequest(
        @NotBlank @Size(max = 30) String username,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        String phone) {
}
