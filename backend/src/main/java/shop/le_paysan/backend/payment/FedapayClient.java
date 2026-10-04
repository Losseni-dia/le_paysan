package shop.le_paysan.backend.payment;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

/**
 * Client HTTP minimal pour l'API Fedapay (cf. docs.fedapay.com, consulté en Phase 5).
 *
 * <p>Note de fiabilité : la doc Fedapay publique ne montre jamais le corps de réponse complet
 * pour POST /transactions. Le parsing ci-dessous accepte à la fois un objet à la racine
 * ({@code {"id": ...}}) et l'ancien format Fedapay v1 encapsulé ({@code {"v1/transaction": {"id": ...}}}).
 * À confirmer définitivement au premier test réel via ngrok (cf. docs/07-plan-execution.md Phase 5).
 */
@Component
public class FedapayClient {

    private final RestClient restClient;

    public FedapayClient(
            @Value("${app.fedapay.base-url}") String baseUrl, @Value("${app.fedapay.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public record CustomerInfo(String firstname, String lastname, String phoneNumber) {
    }

    public record CreatedTransaction(String id) {
    }

    public CreatedTransaction createTransaction(
            String description, BigDecimal amount, String callbackUrl, CustomerInfo customer) {
        Map<String, Object> customerBody = new LinkedHashMap<>();
        customerBody.put("firstname", customer.firstname());
        customerBody.put("lastname", customer.lastname());
        customerBody.put("phone_number", Map.of("number", customer.phoneNumber(), "country", "BJ"));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("description", description);
        body.put("amount", amount.intValue());
        body.put("currency", Map.of("iso", "XOF"));
        body.put("callback_url", callbackUrl);
        body.put("customer", customerBody);

        Map<String, Object> response = post("/transactions", body);
        Map<String, Object> transaction = unwrap(response, "transaction");
        return new CreatedTransaction(String.valueOf(transaction.get("id")));
    }

    public String generatePaymentUrl(String transactionId) {
        Map<String, Object> response = post("/transactions/" + transactionId + "/token", Map.of());
        Object url = response.get("url");
        if (url == null) {
            Map<String, Object> nested = unwrap(response, "token");
            url = nested.get("url");
        }
        if (url == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Fedapay : URL de paiement absente de la réponse");
        }
        return url.toString();
    }

    public record CreatedPayout(String id, String status) {
    }

    public CreatedPayout createPayout(BigDecimal amount, CustomerInfo customer) {
        Map<String, Object> customerBody = new LinkedHashMap<>();
        customerBody.put("firstname", customer.firstname());
        customerBody.put("lastname", customer.lastname());
        customerBody.put("phone_number", Map.of("number", customer.phoneNumber(), "country", "BJ"));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", amount.intValue());
        body.put("currency", Map.of("iso", "XOF"));
        body.put("mode", "mobile_money");
        body.put("customer", customerBody);

        Map<String, Object> response = post("/payouts", body);
        Map<String, Object> payout = unwrap(response, "payout");
        return new CreatedPayout(String.valueOf(payout.get("id")), String.valueOf(payout.get("status")));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String uri, Map<String, Object> body) {
        try {
            return restClient.post().uri(uri).body(body).retrieve().body(Map.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Erreur de communication avec Fedapay : " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> unwrap(Map<String, Object> response, String resourceHint) {
        if (response.containsKey("id")) {
            return response;
        }
        for (Map.Entry<String, Object> entry : response.entrySet()) {
            if (entry.getKey().toLowerCase().contains(resourceHint) && entry.getValue() instanceof Map) {
                return (Map<String, Object>) entry.getValue();
            }
        }
        // Dernier recours : premier champ de type objet trouvé dans la reponse.
        for (Object value : response.values()) {
            if (value instanceof Map) {
                return (Map<String, Object>) value;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Réponse Fedapay inattendue : " + response);
    }
}
