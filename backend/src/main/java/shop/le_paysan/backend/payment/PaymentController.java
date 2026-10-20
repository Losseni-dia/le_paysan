package shop.le_paysan.backend.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping("/api/payments")
    public InitiatePaymentResponse initiate(
            @AuthenticationPrincipal SecurityUser principal, @Valid @RequestBody InitiatePaymentRequest request) {
        return service.initiate(principal.getUser(), request.orderIds(), request.returnUrl());
    }

    /** Appele par Fedapay (jamais par le frontend) : pas de session, auth par signature uniquement. */
    @PostMapping("/api/payments/fedapay/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String rawBody, @RequestHeader(value = "X-FEDAPAY-SIGNATURE", required = false) String signature) {
        service.handleWebhook(rawBody, signature);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
