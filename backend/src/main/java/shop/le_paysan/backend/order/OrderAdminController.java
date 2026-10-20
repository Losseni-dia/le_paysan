package shop.le_paysan.backend.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import shop.le_paysan.backend.order.dto.AdminOrderResponse;

@RestController
@RequestMapping("/api/v1/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class OrderAdminController {

    private final OrderService service;

    public OrderAdminController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public Page<AdminOrderResponse> list(@RequestParam(required = false) String status, Pageable pageable) {
        return service.listForAdmin(status, pageable);
    }

    @GetMapping("/{id}")
    public AdminOrderResponse detail(@PathVariable Long id) {
        return service.getAdminDetail(id);
    }
}
