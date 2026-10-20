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
import shop.le_paysan.backend.review.dto.RatingSummaryResponse;
import shop.le_paysan.backend.review.dto.ReviewEligibilityResponse;
import shop.le_paysan.backend.review.dto.ShopReviewResponse;
import shop.le_paysan.backend.review.dto.SubmitReviewRequest;

@RestController
@RequestMapping("/api/v1/shops/{shopId}/avis")
public class ShopReviewController {

    private final ReviewService service;

    public ShopReviewController(ReviewService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ShopReviewResponse> list(@PathVariable Long shopId, Pageable pageable) {
        return service.listForShop(shopId, pageable);
    }

    @GetMapping("/resume")
    public RatingSummaryResponse summary(@PathVariable Long shopId) {
        return service.getShopRatingSummary(shopId);
    }

    @GetMapping("/moi")
    public ResponseEntity<ShopReviewResponse> mine(@AuthenticationPrincipal SecurityUser principal, @PathVariable Long shopId) {
        ShopReviewResponse review = service.getMyShopReview(principal.getUser(), shopId);
        return review != null ? ResponseEntity.ok(review) : ResponseEntity.noContent().build();
    }

    @GetMapping("/eligibilite")
    public ReviewEligibilityResponse eligibility(@AuthenticationPrincipal SecurityUser principal, @PathVariable Long shopId) {
        return service.getShopEligibility(principal.getUser(), shopId);
    }

    @PostMapping
    public ShopReviewResponse submit(
            @AuthenticationPrincipal SecurityUser principal,
            @PathVariable Long shopId,
            @Valid @RequestBody SubmitReviewRequest request) {
        return service.submitShopReview(principal.getUser(), shopId, request.rating(), request.comment());
    }
}
