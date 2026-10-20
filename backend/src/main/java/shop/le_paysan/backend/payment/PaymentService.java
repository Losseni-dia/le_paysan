package shop.le_paysan.backend.payment;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import shop.le_paysan.backend.common.notification.NotificationService;
import shop.le_paysan.backend.order.Order;
import shop.le_paysan.backend.order.OrderItem;
import shop.le_paysan.backend.order.OrderRepository;
import shop.le_paysan.backend.order.OrderService;
import shop.le_paysan.backend.order.OrderStatusHistory;
import shop.le_paysan.backend.payment.dto.InitiatePaymentResponse;
import shop.le_paysan.backend.product.Product;
import shop.le_paysan.backend.product.ProductRepository;
import shop.le_paysan.backend.user.User;

/**
 * Cf. docs/02-specifications-fonctionnelles.md UC-08 et docs/04-architecture-technique.md §4-5.
 * Création automatique du {@link VendorPayout} désactivée pour le moment : le reversement sera
 * mis en place avec le support Fedapay avant la mise en production.
 */
@Service
public class PaymentService {

    private final PaymentTransactionRepository transactionRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final FedapayClient fedapayClient;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final String frontendUrl;
    private final String webhookSecret;

    public PaymentService(
            PaymentTransactionRepository transactionRepository,
            OrderRepository orderRepository,
            OrderService orderService,
            ProductRepository productRepository,
            FedapayClient fedapayClient,
            NotificationService notificationService,
            ObjectMapper objectMapper,
            @Value("${app.frontend-url}") String frontendUrl,
            @Value("${app.fedapay.webhook-secret}") String webhookSecret) {
        this.transactionRepository = transactionRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.productRepository = productRepository;
        this.fedapayClient = fedapayClient;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.frontendUrl = frontendUrl;
        this.webhookSecret = webhookSecret;
    }

    @Transactional
    public InitiatePaymentResponse initiate(User buyer, List<Long> orderIds) {
        List<Order> orders = orderRepository.findByIdInAndBuyerUserId(orderIds, buyer.getId());
        if (orders.size() != orderIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable");
        }
        for (Order order : orders) {
            if (!Order.STATUS_EN_ATTENTE_PAIEMENT.equals(order.getStatus())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "La commande " + order.getOrderNumber() + " a déjà été traitée");
            }
        }

        if (buyer.getPhone() == null || buyer.getPhone().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Renseignez votre numéro de téléphone dans les paramètres de votre compte avant de payer");
        }

        BigDecimal total = orders.stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        String description = "Le Paysan - commande(s) "
                + orders.stream().map(Order::getOrderNumber).collect(Collectors.joining(", "));

        FedapayClient.CustomerInfo customer =
                new FedapayClient.CustomerInfo(buyer.getFirstName(), buyer.getLastName(), buyer.getPhone());

        FedapayClient.CreatedTransaction created =
                fedapayClient.createTransaction(description, total, frontendUrl + "/commandes/paiement-retour", customer);
        String paymentUrl = fedapayClient.generatePaymentUrl(created.id());

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setBuyerUser(buyer);
        transaction.setFedapayTransactionId(created.id());
        transaction.setAmount(total);
        transaction.setStatus(PaymentTransaction.STATUS_PENDING);
        transaction = transactionRepository.save(transaction);

        for (Order order : orders) {
            order.setPaymentTransaction(transaction);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }

        return new InitiatePaymentResponse(paymentUrl);
    }

    @Transactional
    public void handleWebhook(String rawBody, String signatureHeader) {
        verifySignature(rawBody, signatureHeader);

        JsonNode event = parse(rawBody);
        JsonNode entity = event.has("entity") ? event.get("entity") : event;
        String fedapayTransactionId = textOrNull(entity, "id");
        String fedapayStatus = textOrNull(entity, "status");
        String eventName = textOrNull(event, "name");

        if (fedapayTransactionId == null) {
            return; // payload non reconnu : on ignore plutot que de planter le webhook (Fedapay reessaiera sinon).
        }

        PaymentTransaction transaction = transactionRepository.findByFedapayTransactionId(fedapayTransactionId).orElse(null);
        if (transaction == null || !PaymentTransaction.STATUS_PENDING.equals(transaction.getStatus())) {
            return; // transaction inconnue, ou deja traitee (idempotence du webhook).
        }

        boolean approved = "approved".equalsIgnoreCase(fedapayStatus)
                || (eventName != null && eventName.equalsIgnoreCase("transaction.approved"));
        boolean declinedOrCanceled = "declined".equalsIgnoreCase(fedapayStatus)
                || "canceled".equalsIgnoreCase(fedapayStatus)
                || (eventName != null
                        && (eventName.equalsIgnoreCase("transaction.declined")
                                || eventName.equalsIgnoreCase("transaction.canceled")));

        if (!approved && !declinedOrCanceled) {
            return; // evenement intermediaire non terminal (ex: transaction.created, status "pending") : on attend le webhook final.
        }

        transaction.setRawPayload(rawBody);
        transaction.setUpdatedAt(LocalDateTime.now());

        if (approved) {
            transaction.setStatus(PaymentTransaction.STATUS_APPROVED);
            transactionRepository.save(transaction);
            onPaymentApproved(transaction);
        } else {
            transaction.setStatus(
                    "canceled".equalsIgnoreCase(fedapayStatus) ? PaymentTransaction.STATUS_CANCELED : PaymentTransaction.STATUS_FAILED);
            transactionRepository.save(transaction);
            // Les commandes restent EN_ATTENTE_PAIEMENT : l'acheteur peut reessayer le paiement.
        }
    }

    private void onPaymentApproved(PaymentTransaction transaction) {
        List<Order> orders = orderRepository.findByPaymentTransactionId(transaction.getId());
        for (Order order : orders) {
            order.setStatus(Order.STATUS_PAYEE);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            orderService.recordHistory(order, Order.STATUS_PAYEE, OrderStatusHistory.CHANGED_BY_SYSTEME, "Paiement confirmé par Fedapay");

            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                int remaining = Math.max(0, product.getStockQuantity() - item.getQuantity());
                product.setStockQuantity(remaining);
                productRepository.save(product);
            }

            // Reversement vendeur (VendorPayout) desactive pour le moment : sera mis en place avec
            // le support Fedapay avant la mise en prod (cf. docs/04-architecture-technique.md §5).

            notificationService.notify(
                    order.getBuyerUser(),
                    "ORDER_PAID",
                    "Paiement confirmé",
                    "Votre paiement pour la commande " + order.getOrderNumber() + " a été confirmé.",
                    "/mon-espace/commandes/" + order.getId() + "/facture");

            notificationService.notify(
                    order.getShop().getOwner(),
                    "ORDER_RECEIVED",
                    "Nouvelle commande payée",
                    "Vous avez reçu une nouvelle commande (" + order.getOrderNumber() + ") de "
                            + order.getSubtotalAmount().add(order.getDeliveryAmount()) + " FCFA.",
                    "/vendeur/commandes/" + order.getId());
        }
    }

    /**
     * Format confirme par un vrai webhook recu en sandbox (Phase 5) : l'en-tete
     * X-FEDAPAY-SIGNATURE vaut {@code t=<timestamp_unix>,s=<hmac_sha256_hex>}, ou le hash est
     * calcule sur la chaine {@code "<t>.<corps_brut>"} (meme convention que Stripe).
     */
    private void verifySignature(String rawBody, String signatureHeader) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Webhook Fedapay non configuré (FEDAPAY_WEBHOOK_SECRET manquant)");
        }
        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Signature Fedapay manquante");
        }
        String timestamp = null;
        String providedSignature = null;
        for (String part : signatureHeader.split(",")) {
            String[] kv = part.split("=", 2);
            if (kv.length != 2) {
                continue;
            }
            if ("t".equals(kv[0].trim())) {
                timestamp = kv[1].trim();
            } else if ("s".equals(kv[0].trim())) {
                providedSignature = kv[1].trim();
            }
        }
        if (timestamp == null || providedSignature == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Format de signature Fedapay invalide");
        }
        try {
            String signedPayload = timestamp + "." + rawBody;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] computed = mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
            String computedHex = HexFormat.of().formatHex(computed);
            if (!MessageDigest.isEqual(
                    computedHex.getBytes(StandardCharsets.UTF_8), providedSignature.getBytes(StandardCharsets.UTF_8))) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Signature Fedapay invalide");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Signature Fedapay invalide");
        }
    }

    private JsonNode parse(String rawBody) {
        try {
            return objectMapper.readTree(rawBody);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload webhook Fedapay invalide");
        }
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}
