package shop.le_paysan.backend.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import shop.le_paysan.backend.review.dto.ModerateReviewRequest;
import shop.le_paysan.backend.review.dto.ProductReviewResponse;
import shop.le_paysan.backend.review.dto.ShopReviewResponse;

@RestController
@RequestMapping("/api/admin/avis")
@PreAuthorize("hasRole('ADMIN')")
public class ReviewAdminController {

    private final ReviewService service;

    public ReviewAdminController(ReviewService service) {
        this.service = service;
    }

    @GetMapping("/produits")
    public Page<ProductReviewResponse> listProductReviews(@RequestParam(required = false) String status, Pageable pageable) {
        return service.listProductReviewsForAdmin(status, pageable);
    }

    @GetMapping("/boutiques")
    public Page<ShopReviewResponse> listShopReviews(@RequestParam(required = false) String status, Pageable pageable) {
        return service.listShopReviewsForAdmin(status, pageable);
    }

    @PatchMapping("/produits/{id}")
    public ProductReviewResponse moderateProduct(@PathVariable Long id, @Valid @RequestBody ModerateReviewRequest request) {
        return service.moderateProductReview(id, request.status(), request.reason());
    }

    @PatchMapping("/boutiques/{id}")
    public ShopReviewResponse moderateShop(@PathVariable Long id, @Valid @RequestBody ModerateReviewRequest request) {
        return service.moderateShopReview(id, request.status(), request.reason());
    }
}
