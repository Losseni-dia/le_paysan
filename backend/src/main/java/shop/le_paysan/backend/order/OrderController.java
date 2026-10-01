package shop.le_paysan.backend.order;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping("/api/orders")
    public ResponseEntity<List<OrderResponse>> checkout(@AuthenticationPrincipal SecurityUser principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.checkout(principal.getUser()));
    }

    @GetMapping("/api/orders")
    public List<OrderResponse> listMine(@AuthenticationPrincipal SecurityUser principal) {
        return service.listMine(principal.getUser());
    }
}
