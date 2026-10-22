package shop.le_paysan.backend.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.payment.dto.InitiatePaymentRequest;
import shop.le_paysan.backend.payment.dto.InitiatePaymentResponse;

@RestController
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping("/api/v1/payments")
    public InitiatePaymentResponse initiate(
            @AuthenticationPrincipal SecurityUser principal, @Valid @RequestBody InitiatePaymentRequest request) {
        return service.initiate(principal.getUser(), request.orderIds(), request.returnUrl());
    }

    /**
     * Appele par Fedapay (jamais par le frontend) : pas de session, auth par signature uniquement.
     * Volontairement hors /api/v1 : cette URL est enregistree de maniere statique dans le tableau
     * de bord Fedapay, hors du code. La verser casserait le webhook en prod sans mise a jour
     * manuelle cote Fedapay - elle doit donc rester un contrat d'URL stable, independant des
     * versions de l'API consommee par le frontend/mobile.
     */
    @PostMapping("/api/payments/fedapay/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String rawBody, @RequestHeader(value = "X-FEDAPAY-SIGNATURE", required = false) String signature) {
        service.handleWebhook(rawBody, signature);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Page de redirection servie au navigateur du mobile juste apres le paiement Fedapay (cf.
     * PaymentService.fedapayCallbackUrl : Fedapay exige une URL http(s), pas un deep link
     * personnalise). Redirige immediatement vers le deep link de l'app, qu'Android/iOS intercepte
     * via son intent-filter / universal link pour ramener l'app au premier plan.
     */
    @GetMapping(value = "/api/payments/mobile-return", produces = MediaType.TEXT_HTML_VALUE)
    public String mobileReturnRedirect() {
        return """
                <!DOCTYPE html>
                <html><head><meta charset="utf-8">
                <meta http-equiv="refresh" content="0;url=lepaysan://paiement-retour">
                <script>window.location.replace('lepaysan://paiement-retour');</script>
                </head><body>
                <p>Paiement effectué. Retour à l'application Le Paysan...</p>
                <p><a href="lepaysan://paiement-retour">Cliquez ici si la redirection ne se fait pas automatiquement.</a></p>
                </body></html>
                """;
    }
}
