package shop.le_paysan.backend.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Inscription volontaire livreur (independante de toute boutique) : cf. demande utilisateur. */
public record RegisterLivreurRequest(
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9._-]{3,30}$", message = "Login invalide (3-30 caracteres, lettres/chiffres/._-)")
        String username,

        @NotBlank
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caracteres")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Za-z]).+$", message = "Le mot de passe doit contenir au moins une lettre et un chiffre")
        String password,

        @NotBlank String firstName,
        @NotBlank String lastName,
        String phone,

        @NotBlank(message = "La localité est obligatoire")
        String city,

        @NotNull(message = "La localité est obligatoire")
        java.math.BigDecimal cityLat,

        @NotNull(message = "La localité est obligatoire")
        java.math.BigDecimal cityLng,

        @AssertTrue(message = "L'acceptation des CGU est obligatoire")
        boolean acceptCgu,

        @AssertTrue(message = "L'acceptation des CGV est obligatoire")
        boolean acceptCgv,

        @AssertTrue(message = "La prise de connaissance de la politique de confidentialite est obligatoire")
        boolean acceptPrivacy) {
}
