package shop.le_paysan.backend.cart;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.cart.dto.AddCartItemRequest;
import shop.le_paysan.backend.cart.dto.CartResponse;
import shop.le_paysan.backend.cart.dto.UpdateCartItemRequest;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping
    public CartResponse get(@AuthenticationPrincipal SecurityUser principal) {
        return service.getCart(principal.getUser());
    }

    @PostMapping("/items")
    public CartResponse addItem(@AuthenticationPrincipal SecurityUser principal, @Valid @RequestBody AddCartItemRequest request) {
        return service.addItem(principal.getUser(), request);
    }

    @PutMapping("/items/{productId}")
    public CartResponse updateItem(
            @AuthenticationPrincipal SecurityUser principal,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return service.updateItem(principal.getUser(), productId, request);
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(@AuthenticationPrincipal SecurityUser principal, @PathVariable Long productId) {
        return service.removeItem(principal.getUser(), productId);
    }
}
