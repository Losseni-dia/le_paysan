package shop.le_paysan.backend.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.review.dto.ProductReviewResponse;
import shop.le_paysan.backend.review.dto.RatingSummaryResponse;
import shop.le_paysan.backend.review.dto.ReviewEligibilityResponse;
import shop.le_paysan.backend.review.dto.SubmitReviewRequest;

@RestController
@RequestMapping("/api/v1/products/{productId}/avis")
public class ProductReviewController {

    private final ReviewService service;

    public ProductReviewController(ReviewService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ProductReviewResponse> list(@PathVariable Long productId, Pageable pageable) {
        return service.listForProduct(productId, pageable);
    }

    @GetMapping("/resume")
    public RatingSummaryResponse summary(@PathVariable Long productId) {
        return service.getProductRatingSummary(productId);
    }

    @GetMapping("/moi")
    public ResponseEntity<ProductReviewResponse> mine(
            @AuthenticationPrincipal SecurityUser principal, @PathVariable Long productId) {
        ProductReviewResponse review = service.getMyProductReview(principal.getUser(), productId);
        return review != null ? ResponseEntity.ok(review) : ResponseEntity.noContent().build();
    }

    @GetMapping("/eligibilite")
    public ReviewEligibilityResponse eligibility(
            @AuthenticationPrincipal SecurityUser principal, @PathVariable Long productId) {
        return service.getProductEligibility(principal.getUser(), productId);
    }

    @PostMapping
    public ProductReviewResponse submit(
            @AuthenticationPrincipal SecurityUser principal,
            @PathVariable Long productId,
            @Valid @RequestBody SubmitReviewRequest request) {
        return service.submitProductReview(principal.getUser(), productId, request.rating(), request.comment());
    }
}
