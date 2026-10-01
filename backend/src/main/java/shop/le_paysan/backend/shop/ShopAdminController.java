package shop.le_paysan.backend.shop;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.shop.dto.RejectRequest;
import shop.le_paysan.backend.shop.dto.ShopAdminDetailResponse;
import shop.le_paysan.backend.shop.dto.ShopResponse;

@RestController
@RequestMapping("/api/admin/shops")
@PreAuthorize("hasRole('ADMIN')")
public class ShopAdminController {

    private final ShopService service;

    public ShopAdminController(ShopService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ShopAdminDetailResponse> list(@RequestParam(required = false) String status, Pageable pageable) {
        return service.listForAdmin(status, pageable);
    }

    @GetMapping("/{id}")
    public ShopAdminDetailResponse detail(@PathVariable Long id) {
        return service.getAdminDetail(id);
    }

    @PostMapping("/{id}/validate")
    public ShopResponse validate(@PathVariable Long id, @AuthenticationPrincipal SecurityUser admin) {
        return service.validate(id, admin.getUser());
    }

    @PostMapping("/{id}/reject")
    public ShopResponse reject(
            @PathVariable Long id, @AuthenticationPrincipal SecurityUser admin, @Valid @RequestBody RejectRequest request) {
        return service.reject(id, admin.getUser(), request.reason());
    }

    @PostMapping("/{id}/suspend")
    public ShopResponse suspend(@PathVariable Long id) {
        return service.suspend(id);
    }
}
