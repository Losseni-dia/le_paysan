package shop.le_paysan.backend.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.product.dto.ProductResponse;
import shop.le_paysan.backend.product.dto.RejectProductRequest;

@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class ProductAdminController {

    private final ProductService service;

    public ProductAdminController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ProductResponse> list(@RequestParam(required = false) String status, Pageable pageable) {
        return service.listForAdmin(status, pageable);
    }

    @GetMapping("/{id}")
    public ProductResponse detail(@PathVariable Long id) {
        return service.getAdminDetail(id);
    }

    @PostMapping("/{id}/validate")
    public ProductResponse validate(@PathVariable Long id, @AuthenticationPrincipal SecurityUser admin) {
        return service.validate(id, admin.getUser());
    }

    @PostMapping("/{id}/reject")
    public ProductResponse reject(
            @PathVariable Long id, @AuthenticationPrincipal SecurityUser admin, @Valid @RequestBody RejectProductRequest request) {
        return service.reject(id, admin.getUser(), request.reason());
    }

    @PatchMapping("/{id}/publish-state")
    public ProductResponse togglePublishState(@PathVariable Long id, @AuthenticationPrincipal SecurityUser admin) {
        return service.adminTogglePublishState(id, admin.getUser());
    }
}
