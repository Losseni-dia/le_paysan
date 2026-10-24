package shop.le_paysan.backend.user.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Passage d'un compte existant au role livreur (cf. demande utilisateur : pas de
 * nouvelle inscription ni de saisie redondante du nom/prenom - seulement la
 * localite et les acceptations specifiques au role livreur).
 */
public record BecomeLivreurRequest(
        @NotBlank(message = "La localité est obligatoire") String city,
        @NotNull(message = "La localité est obligatoire") java.math.BigDecimal cityLat,
        @NotNull(message = "La localité est obligatoire") java.math.BigDecimal cityLng,
        @AssertTrue(message = "L'acceptation des CGU est obligatoire") boolean acceptCgu,
        @AssertTrue(message = "L'acceptation des CGV est obligatoire") boolean acceptCgv,
        @AssertTrue(message = "La prise de connaissance de la politique de confidentialite est obligatoire")
                boolean acceptPrivacy) {
}
