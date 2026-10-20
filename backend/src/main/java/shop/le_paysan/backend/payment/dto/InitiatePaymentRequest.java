package shop.le_paysan.backend.payment.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

/**
 * returnUrl : optionnel, URL vers laquelle Fedapay redirige une fois le paiement termine. Absent
 * (client web actuel) -> fallback sur {@code app.frontend-url}/commandes/paiement-retour. Permet a
 * un client mobile de fournir son propre deep link (cf. {@code app.mobile-deeplink-scheme}).
 */
public record InitiatePaymentRequest(@NotEmpty List<Long> orderIds, String returnUrl) {
}
