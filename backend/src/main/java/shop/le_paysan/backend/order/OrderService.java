package shop.le_paysan.backend.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.cart.Cart;
import shop.le_paysan.backend.cart.CartItem;
import shop.le_paysan.backend.cart.CartItemRepository;
import shop.le_paysan.backend.cart.CartRepository;
import shop.le_paysan.backend.commission.CommissionSettingsRepository;
import shop.le_paysan.backend.order.dto.OrderResponse;
import shop.le_paysan.backend.product.Product;
import shop.le_paysan.backend.shop.Shop;
import shop.le_paysan.backend.user.User;

@Service
public class OrderService {

    private static final BigDecimal DEFAULT_COMMISSION_RATE = BigDecimal.valueOf(10);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final CommissionSettingsRepository commissionSettingsRepository;
    private final Random random = new Random();

    public OrderService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            OrderRepository orderRepository,
            CommissionSettingsRepository commissionSettingsRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.commissionSettingsRepository = commissionSettingsRepository;
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

            createdOrders.add(orderRepository.save(order));
        }

        cartItemRepository.deleteAll(cartItems);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return createdOrders.stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listMine(User buyer) {
        return orderRepository.findByBuyerUserIdOrderByCreatedAtDesc(buyer.getId()).stream()
                .map(OrderResponse::from)
                .toList();
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
