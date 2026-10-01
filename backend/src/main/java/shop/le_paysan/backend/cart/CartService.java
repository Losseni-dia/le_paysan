package shop.le_paysan.backend.cart;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.cart.dto.AddCartItemRequest;
import shop.le_paysan.backend.cart.dto.CartItemResponse;
import shop.le_paysan.backend.cart.dto.CartResponse;
import shop.le_paysan.backend.cart.dto.UpdateCartItemRequest;
import shop.le_paysan.backend.product.Product;
import shop.le_paysan.backend.product.ProductRepository;
import shop.le_paysan.backend.shop.Shop;
import shop.le_paysan.backend.user.User;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(User user) {
        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart == null) {
            return CartResponse.from(java.util.List.of());
        }
        // Lecture directe plutot que cart.getItems() : evite une collection Hibernate
        // restee obsolete dans le contexte de persistance lorsque getCart() est appele
        // juste apres une mutation faite via CartItemRepository (meme transaction).
        List<CartItem> items = cartItemRepository.findByCartIdOrderByIdAsc(cart.getId());
        return CartResponse.from(items.stream().map(CartItemResponse::from).toList());
    }

    @Transactional
    public CartResponse addItem(User user, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository
                .findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit introuvable"));
        validatePurchasable(product);
        validateDeliveryMode(product, request.deliveryMode(), request.deliveryAddress());

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElseGet(() -> {
                    CartItem created = new CartItem();
                    created.setCart(cart);
                    created.setProduct(product);
                    created.setQuantity(0);
                    return created;
                });

        int newQuantity = item.getQuantity() + request.quantity();
        if (newQuantity > product.getStockQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantité demandée supérieure au stock disponible");
        }
        item.setQuantity(newQuantity);
        item.setDeliveryMode(request.deliveryMode());
        item.setDeliveryAddress(request.deliveryAddress());
        cartItemRepository.save(item);

        touchCart(cart);
        return getCart(user);
    }

    @Transactional
    public CartResponse updateItem(User user, Long productId, UpdateCartItemRequest request) {
        Cart cart = cartRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Panier vide"));
        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit absent du panier"));

        Product product = item.getProduct();
        validateDeliveryMode(product, request.deliveryMode(), request.deliveryAddress());
        if (request.quantity() > product.getStockQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantité demandée supérieure au stock disponible");
        }

        item.setQuantity(request.quantity());
        item.setDeliveryMode(request.deliveryMode());
        item.setDeliveryAddress(request.deliveryAddress());
        cartItemRepository.save(item);

        touchCart(cart);
        return getCart(user);
    }

    @Transactional
    public CartResponse removeItem(User user, Long productId) {
        Cart cart = cartRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Panier vide"));
        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
        touchCart(cart);
        return getCart(user);
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    private void touchCart(Cart cart) {
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    private void validatePurchasable(Product product) {
        if (!Product.STATUS_VALIDE.equals(product.getStatus()) || !Shop.STATUS_VALIDEE.equals(product.getShop().getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce produit n'est plus disponible à l'achat");
        }
    }

    private void validateDeliveryMode(Product product, String deliveryMode, String deliveryAddress) {
        boolean offered = product.getDeliveryOptions().stream().anyMatch(o -> o.getMode().equals(deliveryMode));
        if (!offered) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mode de livraison non proposé pour ce produit");
        }
        if ("LIVRAISON_DOMICILE".equals(deliveryMode) && (deliveryAddress == null || deliveryAddress.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'adresse de livraison est obligatoire");
        }
    }
}
