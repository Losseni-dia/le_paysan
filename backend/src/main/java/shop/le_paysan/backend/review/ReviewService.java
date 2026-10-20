package shop.le_paysan.backend.review;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.common.notification.NotificationService;
import shop.le_paysan.backend.order.Order;
import shop.le_paysan.backend.order.OrderItem;
import shop.le_paysan.backend.order.OrderItemRepository;
import shop.le_paysan.backend.order.OrderRepository;
import shop.le_paysan.backend.product.Product;
import shop.le_paysan.backend.product.ProductRepository;
import shop.le_paysan.backend.review.dto.ProductReviewResponse;
import shop.le_paysan.backend.review.dto.RatingSummaryResponse;
import shop.le_paysan.backend.review.dto.ReviewEligibilityResponse;
import shop.le_paysan.backend.review.dto.ShopReviewResponse;
import shop.le_paysan.backend.shop.Shop;
import shop.le_paysan.backend.shop.ShopRepository;
import shop.le_paysan.backend.user.User;

/** Cf. docs/02-specifications-fonctionnelles.md UC-11 : avis vérifié achat, publié immédiatement, modérable. */
@Service
public class ReviewService {

    private final ProductReviewRepository productReviewRepository;
    private final ShopReviewRepository shopReviewRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public ReviewService(
            ProductReviewRepository productReviewRepository,
            ShopReviewRepository shopReviewRepository,
            ProductRepository productRepository,
            ShopRepository shopRepository,
            OrderItemRepository orderItemRepository,
            OrderRepository orderRepository,
            NotificationService notificationService) {
        this.productReviewRepository = productReviewRepository;
        this.shopReviewRepository = shopReviewRepository;
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    // --- Produit ---

    @Transactional(readOnly = true)
    public Page<ProductReviewResponse> listForProduct(Long productId, Pageable pageable) {
        return productReviewRepository
                .findByProductIdAndStatusOrderByCreatedAtDesc(productId, ProductReview.STATUS_VISIBLE, pageable)
                .map(ProductReviewResponse::from);
    }

    @Transactional(readOnly = true)
    public RatingSummaryResponse getProductRatingSummary(Long productId) {
        Double avg = productReviewRepository.averageRating(productId);
        long count = productReviewRepository.countByProductIdAndStatus(productId, ProductReview.STATUS_VISIBLE);
        return new RatingSummaryResponse(avg != null ? avg : 0.0, count);
    }

    @Transactional(readOnly = true)
    public ProductReviewResponse getMyProductReview(User buyer, Long productId) {
        return productReviewRepository
                .findByProductIdAndBuyerUserId(productId, buyer.getId())
                .map(ProductReviewResponse::from)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public ReviewEligibilityResponse getProductEligibility(User buyer, Long productId) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit introuvable"));
        if (product.getShop().getOwner().getId().equals(buyer.getId())) {
            return ReviewEligibilityResponse.ofIneligible(ReviewEligibilityResponse.REASON_OWNER);
        }
        boolean purchased = orderItemRepository
                .findFirstByProduct_IdAndOrder_BuyerUser_IdAndOrder_StatusOrderByOrder_CreatedAtDesc(
                        productId, buyer.getId(), Order.STATUS_LIVREE)
                .isPresent();
        return purchased
                ? ReviewEligibilityResponse.ofEligible()
                : ReviewEligibilityResponse.ofIneligible(ReviewEligibilityResponse.REASON_NOT_PURCHASED);
    }

    @Transactional
    public ProductReviewResponse submitProductReview(User buyer, Long productId, Integer rating, String comment) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit introuvable"));
        if (product.getShop().getOwner().getId().equals(buyer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez pas noter votre propre produit");
        }
        OrderItem purchase = orderItemRepository
                .findFirstByProduct_IdAndOrder_BuyerUser_IdAndOrder_StatusOrderByOrder_CreatedAtDesc(
                        productId, buyer.getId(), Order.STATUS_LIVREE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Vous devez avoir reçu ce produit pour pouvoir le noter"));

        ProductReview review = productReviewRepository
                .findByProductIdAndBuyerUserId(productId, buyer.getId())
                .orElseGet(ProductReview::new);
        review.setProduct(product);
        review.setBuyerUser(buyer);
        review.setOrderItem(purchase);
        review.setRating(rating);
        review.setComment(comment);
        review.setStatus(ProductReview.STATUS_VISIBLE);
        review.setModerationReason(null);
        review.setUpdatedAt(LocalDateTime.now());
        boolean isNew = review.getId() == null;
        review = productReviewRepository.save(review);

        if (isNew) {
            notificationService.notify(
                    product.getShop().getOwner(),
                    "REVIEW_RECEIVED",
                    "Nouvel avis reçu",
                    "Votre produit \"" + product.getName() + "\" a reçu une note de " + rating + "/5.",
                    "/vendeur/produits/" + product.getId() + "/modifier");
        }

        return ProductReviewResponse.from(review);
    }

    // --- Boutique ---

    @Transactional(readOnly = true)
    public Page<ShopReviewResponse> listForShop(Long shopId, Pageable pageable) {
        return shopReviewRepository
                .findByShopIdAndStatusOrderByCreatedAtDesc(shopId, ShopReview.STATUS_VISIBLE, pageable)
                .map(ShopReviewResponse::from);
    }

    @Transactional(readOnly = true)
    public RatingSummaryResponse getShopRatingSummary(Long shopId) {
        Double avg = shopReviewRepository.averageRating(shopId);
        long count = shopReviewRepository.countByShopIdAndStatus(shopId, ShopReview.STATUS_VISIBLE);
        return new RatingSummaryResponse(avg != null ? avg : 0.0, count);
    }

    @Transactional(readOnly = true)
    public ShopReviewResponse getMyShopReview(User buyer, Long shopId) {
        return shopReviewRepository
                .findByShopIdAndBuyerUserId(shopId, buyer.getId())
                .map(ShopReviewResponse::from)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public ReviewEligibilityResponse getShopEligibility(User buyer, Long shopId) {
        Shop shop = shopRepository
                .findById(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Boutique introuvable"));
        if (shop.getOwner().getId().equals(buyer.getId())) {
            return ReviewEligibilityResponse.ofIneligible(ReviewEligibilityResponse.REASON_OWNER);
        }
        boolean purchased = orderRepository
                .findFirstByShopIdAndBuyerUserIdAndStatusOrderByCreatedAtDesc(shopId, buyer.getId(), Order.STATUS_LIVREE)
                .isPresent();
        return purchased
                ? ReviewEligibilityResponse.ofEligible()
                : ReviewEligibilityResponse.ofIneligible(ReviewEligibilityResponse.REASON_NOT_PURCHASED);
    }

    @Transactional
    public ShopReviewResponse submitShopReview(User buyer, Long shopId, Integer rating, String comment) {
        Shop shop = shopRepository
                .findById(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Boutique introuvable"));
        if (shop.getOwner().getId().equals(buyer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez pas noter votre propre boutique");
        }
        Order purchase = orderRepository
                .findFirstByShopIdAndBuyerUserIdAndStatusOrderByCreatedAtDesc(shopId, buyer.getId(), Order.STATUS_LIVREE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Vous devez avoir reçu une commande de cette boutique pour pouvoir la noter"));

        ShopReview review = shopReviewRepository
                .findByShopIdAndBuyerUserId(shopId, buyer.getId())
                .orElseGet(ShopReview::new);
        review.setShop(shop);
        review.setBuyerUser(buyer);
        review.setOrder(purchase);
        review.setRating(rating);
        review.setComment(comment);
        review.setStatus(ShopReview.STATUS_VISIBLE);
        review.setModerationReason(null);
        review.setUpdatedAt(LocalDateTime.now());
        boolean isNew = review.getId() == null;
        review = shopReviewRepository.save(review);

        if (isNew) {
            notificationService.notify(
                    shop.getOwner(),
                    "REVIEW_RECEIVED",
                    "Nouvel avis reçu",
                    "Votre boutique \"" + shop.getName() + "\" a reçu une note de " + rating + "/5.",
                    "/vendeur/ma-boutique");
        }

        return ShopReviewResponse.from(review);
    }

    // --- Admin ---

    @Transactional(readOnly = true)
    public Page<ProductReviewResponse> listProductReviewsForAdmin(String status, Pageable pageable) {
        Page<ProductReview> page = status != null
                ? productReviewRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                : productReviewRepository.findAll(pageable);
        return page.map(ProductReviewResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ShopReviewResponse> listShopReviewsForAdmin(String status, Pageable pageable) {
        Page<ShopReview> page = status != null
                ? shopReviewRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                : shopReviewRepository.findAll(pageable);
        return page.map(ShopReviewResponse::from);
    }

    @Transactional
    public ProductReviewResponse moderateProductReview(Long id, String status, String reason) {
        ProductReview review = productReviewRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validateStatus(status);
        review.setStatus(status);
        review.setModerationReason(reason);
        review.setUpdatedAt(LocalDateTime.now());
        return ProductReviewResponse.from(productReviewRepository.save(review));
    }

    @Transactional
    public ShopReviewResponse moderateShopReview(Long id, String status, String reason) {
        ShopReview review = shopReviewRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validateStatus(status);
        review.setStatus(status);
        review.setModerationReason(reason);
        review.setUpdatedAt(LocalDateTime.now());
        return ShopReviewResponse.from(shopReviewRepository.save(review));
    }

    private static void validateStatus(String status) {
        if (!ProductReview.STATUS_VISIBLE.equals(status) && !ProductReview.STATUS_MASQUE.equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Statut invalide");
        }
    }
}
