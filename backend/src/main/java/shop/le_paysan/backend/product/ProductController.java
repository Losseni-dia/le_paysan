package shop.le_paysan.backend.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.product.dto.ProductCreateForm;
import shop.le_paysan.backend.product.dto.ProductResponse;

@RestController
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/products")
    public Page<ProductResponse> listPublic(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) String deliveryMode,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double refLat,
            @RequestParam(required = false) Double refLng,
            @RequestParam(required = false) String refCity,
            Pageable pageable) {
        return service.listPublic(categoryId, shopId, deliveryMode, search, refLat, refLng, refCity, pageable);
    }

    @GetMapping("/api/v1/products/{id}")
    public ProductResponse getPublic(@PathVariable Long id) {
        return service.getPublic(id);
    }

    @PostMapping(value = "/api/v1/vendor/products", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('VENDEUR')")
    public ResponseEntity<ProductResponse> create(
            @AuthenticationPrincipal SecurityUser principal, @ModelAttribute ProductCreateForm form) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(principal.getUser(), form));
    }

    @PutMapping(value = "/api/v1/vendor/products/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('VENDEUR')")
    public ProductResponse update(
            @AuthenticationPrincipal SecurityUser principal, @PathVariable Long id, @ModelAttribute ProductCreateForm form) {
        return service.update(principal.getUser(), id, form);
    }

    @PatchMapping("/api/v1/vendor/products/{id}/publish-state")
    @PreAuthorize("hasRole('VENDEUR')")
    public ProductResponse togglePublishState(@AuthenticationPrincipal SecurityUser principal, @PathVariable Long id) {
        return service.togglePublishState(principal.getUser(), id);
    }

    @GetMapping("/api/v1/vendor/products")
    @PreAuthorize("hasRole('VENDEUR')")
    public Page<ProductResponse> listMine(@AuthenticationPrincipal SecurityUser principal, Pageable pageable) {
        return service.listMine(principal.getUser(), pageable);
    }
}
