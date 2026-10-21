package shop.le_paysan.backend.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Cf. docs/02-specifications-fonctionnelles.md UC-01. */
public record RegisterRequest(
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9._-]{3,30}$", message = "Login invalide (3-30 caracteres, lettres/chiffres/._-)")
        String username,

        @NotBlank
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caracteres")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Za-z]).+$", message = "Le mot de passe doit contenir au moins une lettre et un chiffre")
        String password,

        @Email
        String email,

        @NotBlank String firstName,
        @NotBlank String lastName,
        String phone,
        String city,

        @AssertTrue(message = "L'acceptation des CGU est obligatoire")
        boolean acceptCgu,

        @AssertTrue(message = "L'acceptation des CGV est obligatoire")
        boolean acceptCgv,

        @AssertTrue(message = "La prise de connaissance de la politique de confidentialite est obligatoire")
        boolean acceptPrivacy) {
}
