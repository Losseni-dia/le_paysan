package shop.le_paysan.backend.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.cart.Cart;
import shop.le_paysan.backend.cart.CartItem;
import shop.le_paysan.backend.cart.CartItemRepository;
import shop.le_paysan.backend.cart.CartRepository;
import shop.le_paysan.backend.commission.CommissionSettingsRepository;
import shop.le_paysan.backend.common.notification.NotificationService;
import shop.le_paysan.backend.livreur.LivreurService;
import shop.le_paysan.backend.order.dto.AdminOrderResponse;
import shop.le_paysan.backend.order.dto.OrderResponse;
import shop.le_paysan.backend.order.dto.OrderStatusHistoryResponse;
import shop.le_paysan.backend.product.Product;
import shop.le_paysan.backend.product.ProductDeliveryOption;
import shop.le_paysan.backend.shop.Shop;
import shop.le_paysan.backend.user.User;

@Service
public class OrderService {

    private static final BigDecimal DEFAULT_COMMISSION_RATE = BigDecimal.valueOf(10);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final CommissionSettingsRepository commissionSettingsRepository;
    private final NotificationService notificationService;
    private final LivreurService livreurService;
    private final Random random = new Random();

    public OrderService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            OrderRepository orderRepository,
            OrderStatusHistoryRepository historyRepository,
            CommissionSettingsRepository commissionSettingsRepository,
            NotificationService notificationService,
            LivreurService livreurService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.commissionSettingsRepository = commissionSettingsRepository;
        this.notificationService = notificationService;
        this.livreurService = livreurService;
    }

    /** Ecrit une ligne d'historique pour la frise de suivi (cf. plan "timeline de commande"). */
    @Transactional
    public void recordHistory(Order order, String status, String changedBy, String note) {
        recordHistory(order, status, changedBy, note, OrderStatusHistory.PROOF_NONE, null);
    }

    @Transactional
    public void recordHistory(Order order, String status, String changedBy, String note, String proofType, String proofUrl) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(status);
        history.setChangedBy(changedBy);
        history.setNote(note);
        history.setProofType(proofType);
        history.setProofUrl(proofUrl);
        historyRepository.save(history);
    }

    @Transactional
    public List<OrderResponse> checkout(User buyer) {
        Cart cart = cartRepository
                .findByUserId(buyer.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Votre panier est vide"));
        List<CartItem> cartItems = cartItemRepository.findByCartIdOrderByIdAsc(cart.getId());
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Votre panier est vide");
        }

        for (CartItem item : cartItems) {
            validateStillPurchasable(item);
        }

        BigDecimal commissionRate = commissionSettingsRepository
                .findFirstByCategoryIsNullAndActiveTrueOrderByCreatedAtDesc()
                .map(s -> s.getRate())
                .orElse(DEFAULT_COMMISSION_RATE);

        Map<Long, List<CartItem>> itemsByShop = new LinkedHashMap<>();
        for (CartItem item : cartItems) {
            itemsByShop.computeIfAbsent(item.getProduct().getShop().getId(), k -> new java.util.ArrayList<>()).add(item);
        }

        List<Order> createdOrders = new java.util.ArrayList<>();
        for (List<CartItem> shopItems : itemsByShop.values()) {
            Shop shop = shopItems.get(0).getProduct().getShop();

            Order order = new Order();
            order.setOrderNumber(generateOrderNumber());
            order.setBuyerUser(buyer);
            order.setShop(shop);
            order.setStatus(Order.STATUS_EN_ATTENTE_PAIEMENT);

            BigDecimal subtotal = BigDecimal.ZERO;
            BigDecimal delivery = BigDecimal.ZERO;

            for (CartItem cartItem : shopItems) {
                Product product = cartItem.getProduct();
                BigDecimal lineSubtotal = product.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                BigDecimal deliveryPrice = product.getDeliveryOptions().stream()
                        .filter(o -> o.getMode().equals(cartItem.getDeliveryMode()))
                        .findFirst()
                        .map(o -> o.getDeliveryPrice() == null ? BigDecimal.ZERO : o.getDeliveryPrice())
                        .orElse(BigDecimal.ZERO);

                subtotal = subtotal.add(lineSubtotal);
                delivery = delivery.add(deliveryPrice);

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setProductNameSnapshot(product.getName());
                orderItem.setUnitPriceSnapshot(product.getUnitPrice());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setDeliveryMode(cartItem.getDeliveryMode());
                orderItem.setDeliveryPrice(deliveryPrice);
                orderItem.setDeliveryAddress(cartItem.getDeliveryAddress());
                order.getItems().add(orderItem);
            }

            BigDecimal commissionAmount = subtotal.multiply(commissionRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            order.setSubtotalAmount(subtotal);
            order.setDeliveryAmount(delivery);
            order.setTotalAmount(subtotal.add(delivery));
            order.setCommissionRate(commissionRate);
            order.setCommissionAmount(commissionAmount);

            Order saved = orderRepository.save(order);
            recordHistory(saved, Order.STATUS_EN_ATTENTE_PAIEMENT, OrderStatusHistory.CHANGED_BY_SYSTEME, "Commande créée");
            createdOrders.add(saved);
        }

        cartItemRepository.deleteAll(cartItems);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return createdOrders.stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listMine(User buyer) {
        return orderRepository.findByBuyerUserIdOrderByCreatedAtDesc(buyer.getId()).stream()
                .map(order -> OrderResponse.from(order, historyOf(order.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getMine(User buyer, Long orderId) {
        Order order = orderRepository
                .findByIdAndBuyerUserId(orderId, buyer.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable"));
        return OrderResponse.from(order, historyOf(order.getId()));
    }

    @Transactional
    public OrderResponse confirmReceptionByBuyer(User buyer, Long orderId) {
        Order order = orderRepository
                .findByIdAndBuyerUserId(orderId, buyer.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable"));
        if (!Order.STATUS_PRETE.equals(order.getStatus()) && !Order.STATUS_EXPEDIEE.equals(order.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Cette commande ne peut pas encore être confirmée comme reçue");
        }
        order.setStatus(Order.STATUS_LIVREE);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        recordHistory(order, Order.STATUS_LIVREE, OrderStatusHistory.CHANGED_BY_ACHETEUR, "Réception confirmée par l'acheteur");

        notificationService.notify(
                order.getShop().getOwner(),
                "ORDER_STATUS_CHANGED",
                "Commande " + order.getOrderNumber(),
                "L'acheteur a confirmé la réception de la commande " + order.getOrderNumber() + ".",
                "/vendeur/commandes");

        return OrderResponse.from(order, historyOf(order.getId()));
    }

    private List<OrderStatusHistoryResponse> historyOf(Long orderId) {
        return historyRepository.findByOrderIdOrderByChangedAtAsc(orderId).stream()
                .map(OrderStatusHistoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AdminOrderResponse> listForAdmin(String status, Pageable pageable) {
        Page<Order> orders = status != null
                ? orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                : orderRepository.findAllByOrderByCreatedAtDesc(pageable);
        return orders.map(order -> AdminOrderResponse.from(order, historyOf(order.getId())));
    }

    @Transactional(readOnly = true)
    public AdminOrderResponse getAdminDetail(Long orderId) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable"));
        return AdminOrderResponse.from(order, historyOf(order.getId()));
    }

    @Transactional(readOnly = true)
    public Page<AdminOrderResponse> listForVendor(Shop shop, String status, Pageable pageable) {
        Page<Order> orders = status != null
                ? orderRepository.findByShopIdAndStatusOrderByCreatedAtDesc(shop.getId(), status, pageable)
                : orderRepository.findByShopIdOrderByCreatedAtDesc(shop.getId(), pageable);
        return orders.map(order -> AdminOrderResponse.from(order, historyOf(order.getId())));
    }

    @Transactional(readOnly = true)
    public AdminOrderResponse getForVendor(Shop shop, Long orderId) {
        Order order = findOwnedByShop(shop, orderId);
        return AdminOrderResponse.from(order, historyOf(order.getId()));
    }

    /**
     * Cf. remarque utilisateur (plan "timeline de commande") : le passage PRETE/EXPEDIEE n'est pas un
     * choix du vendeur, c'est deja determine par le mode de livraison choisi par l'acheteur (lui-meme
     * contraint par les options definies par le vendeur sur chaque produit). On le deduit ici plutot
     * que de laisser le vendeur "choisir" entre les deux.
     */
    private String computeNextStatus(Order order) {
        return switch (order.getStatus()) {
            case Order.STATUS_PAYEE -> Order.STATUS_EN_PREPARATION;
            case Order.STATUS_EN_PREPARATION -> order.getItems().stream()
                    .anyMatch(item -> ProductDeliveryOption.MODE_LIVRAISON.equals(item.getDeliveryMode()))
                    ? Order.STATUS_EXPEDIEE
                    : Order.STATUS_PRETE;
            case Order.STATUS_PRETE, Order.STATUS_EXPEDIEE -> Order.STATUS_LIVREE;
            default -> null;
        };
    }

    @Transactional
    public AdminOrderResponse markNextStatusForVendor(
            Shop shop, Long orderId, Long livreurId, String proofType, String proofUrl) {
        Order order = findOwnedByShop(shop, orderId);
        String newStatus = computeNextStatus(order);
        if (newStatus == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Aucune transition possible depuis le statut " + order.getStatus());
        }
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        // Cf. remarque utilisateur (plan Phase 2) : seules les commandes confirmees EXPEDIEE
        // (livraison a domicile) peuvent etre assignees a un livreur, pas le retrait boutique.
        if (Order.STATUS_EXPEDIEE.equals(newStatus) && livreurId != null) {
            order.setAssignedLivreur(livreurService.requireBelongsToShop(shop, livreurId));
        }

        orderRepository.save(order);

        // Cf. remarque utilisateur : le vendeur peut livrer lui-meme (pas de livreur assigne) et
        // doit pouvoir joindre la meme preuve (signature/photo) qu'un livreur, de maniere optionnelle.
        boolean hasProof = proofUrl != null;
        recordHistory(
                order,
                newStatus,
                OrderStatusHistory.CHANGED_BY_VENDEUR,
                Order.STATUS_LIVREE.equals(newStatus)
                        ? "Livraison effectuée par le vendeur" + (hasProof ? " (avec preuve)" : "")
                        : null,
                hasProof ? proofType : OrderStatusHistory.PROOF_NONE,
                proofUrl);

        notificationService.notify(
                order.getBuyerUser(),
                "ORDER_STATUS_CHANGED",
                "Commande " + order.getOrderNumber(),
                "Le statut de votre commande " + order.getOrderNumber() + " est passé à " + newStatus + ".",
                "/mon-espace/commandes");

        return AdminOrderResponse.from(order, historyOf(order.getId()));
    }

    @Transactional(readOnly = true)
    public Page<AdminOrderResponse> listForLivreur(User livreur, Pageable pageable) {
        return orderRepository
                .findByAssignedLivreurIdAndStatusOrderByCreatedAtDesc(livreur.getId(), Order.STATUS_EXPEDIEE, pageable)
                .map(order -> AdminOrderResponse.from(order, historyOf(order.getId())));
    }

    @Transactional(readOnly = true)
    public AdminOrderResponse getForLivreur(User livreur, Long orderId) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable"));
        if (order.getAssignedLivreur() == null || !order.getAssignedLivreur().getId().equals(livreur.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable");
        }
        return AdminOrderResponse.from(order, historyOf(order.getId()));
    }

    @Transactional
    public AdminOrderResponse confirmDeliveryByLivreur(User livreur, Long orderId, String proofType, String proofUrl) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable"));
        if (order.getAssignedLivreur() == null || !order.getAssignedLivreur().getId().equals(livreur.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable");
        }
        if (!Order.STATUS_EXPEDIEE.equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette commande n'est pas en attente de remise");
        }

        order.setStatus(Order.STATUS_LIVREE);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        recordHistory(order, Order.STATUS_LIVREE, OrderStatusHistory.CHANGED_BY_LIVREUR, "Remise confirmée par le livreur", proofType, proofUrl);

        notificationService.notify(
                order.getBuyerUser(),
                "ORDER_STATUS_CHANGED",
                "Commande livrée",
                "Votre commande " + order.getOrderNumber() + " a été remise par le livreur.",
                "/mon-espace/commandes");
        notificationService.notify(
                order.getShop().getOwner(),
                "ORDER_STATUS_CHANGED",
                "Commande livrée",
                "La commande " + order.getOrderNumber() + " a été remise par le livreur.",
                "/vendeur/commandes");

        return AdminOrderResponse.from(order, historyOf(order.getId()));
    }

    private Order findOwnedByShop(Shop shop, Long orderId) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable"));
        if (!order.getShop().getId().equals(shop.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable");
        }
        return order;
    }

    private void validateStillPurchasable(CartItem item) {
        Product product = item.getProduct();
        if (!Product.STATUS_VALIDE.equals(product.getStatus()) || !Shop.STATUS_VALIDEE.equals(product.getShop().getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Le produit \"" + product.getName() + "\" n'est plus disponible, retirez-le du panier");
        }
        if (item.getQuantity() > product.getStockQuantity()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Stock insuffisant pour \"" + product.getName() + "\", ajustez la quantité");
        }
        boolean modeStillOffered =
                product.getDeliveryOptions().stream().anyMatch(o -> o.getMode().equals(item.getDeliveryMode()));
        if (!modeStillOffered) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Le mode de livraison choisi pour \"" + product.getName() + "\" n'est plus proposé");
        }
    }

    private String generateOrderNumber() {
        String candidate;
        do {
            candidate = "CMD" + (100000 + random.nextInt(900000));
        } while (orderRepository.existsByOrderNumber(candidate));
        return candidate;
    }
}
