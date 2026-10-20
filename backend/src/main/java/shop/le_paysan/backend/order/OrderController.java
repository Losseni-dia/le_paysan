package shop.le_paysan.backend.order;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;



import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.order.dto.OrderResponse;

@RestController
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping("/api/v1/orders")
    public ResponseEntity<List<OrderResponse>> checkout(@AuthenticationPrincipal SecurityUser principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.checkout(principal.getUser()));
    }

    @GetMapping("/api/v1/orders")
    public List<OrderResponse> listMine(@AuthenticationPrincipal SecurityUser principal) {
        return service.listMine(principal.getUser());
    }

    @GetMapping("/api/v1/orders/{id}")
    public OrderResponse getMine(@AuthenticationPrincipal SecurityUser principal, @PathVariable Long id) {
        return service.getMine(principal.getUser(), id);
    }

    @PostMapping("/api/v1/orders/{id}/confirmer-reception")
    public OrderResponse confirmReception(@AuthenticationPrincipal SecurityUser principal, @PathVariable Long id) {
        return service.confirmReceptionByBuyer(principal.getUser(), id);
    }
}
