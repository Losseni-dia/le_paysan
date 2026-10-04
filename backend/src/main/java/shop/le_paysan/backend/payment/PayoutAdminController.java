package shop.le_paysan.backend.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import shop.le_paysan.backend.payment.dto.VendorPayoutResponse;

@RestController
@RequestMapping("/api/admin/payouts")
@PreAuthorize("hasRole('ADMIN')")
public class PayoutAdminController {

    private final PayoutService service;

    public PayoutAdminController(PayoutService service) {
        this.service = service;
    }

    @GetMapping
    public Page<VendorPayoutResponse> list(@RequestParam(required = false) String status, Pageable pageable) {
        return service.listForAdmin(status, pageable).map(VendorPayoutResponse::from);
    }

    @PostMapping("/{id}/retry")
    public void retry(@PathVariable Long id) {
        service.retry(id);
    }
}
