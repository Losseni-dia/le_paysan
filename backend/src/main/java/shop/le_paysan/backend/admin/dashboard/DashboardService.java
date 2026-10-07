package shop.le_paysan.backend.admin.dashboard;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import shop.le_paysan.backend.admin.dashboard.dto.DashboardResponse;
import shop.le_paysan.backend.admin.dashboard.dto.ShopRevenueResponse;
import shop.le_paysan.backend.admin.dashboard.dto.StatsResponse;
import shop.le_paysan.backend.order.OrderRepository;
import shop.le_paysan.backend.payment.VendorPayout;
import shop.le_paysan.backend.payment.VendorPayoutRepository;
import shop.le_paysan.backend.product.Product;
import shop.le_paysan.backend.product.ProductRepository;
import shop.le_paysan.backend.review.ProductReview;
import shop.le_paysan.backend.review.ProductReviewRepository;
import shop.le_paysan.backend.review.ShopReview;
import shop.le_paysan.backend.review.ShopReviewRepository;
import shop.le_paysan.backend.shop.Shop;
import shop.le_paysan.backend.shop.ShopRepository;
import shop.le_paysan.backend.user.UserRepository;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final VendorPayoutRepository payoutRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ShopReviewRepository shopReviewRepository;

    public DashboardService(
            UserRepository userRepository,
            ShopRepository shopRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            VendorPayoutRepository payoutRepository,
            ProductReviewRepository productReviewRepository,
            ShopReviewRepository shopReviewRepository) {
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.payoutRepository = payoutRepository;
        this.productReviewRepository = productReviewRepository;
        this.shopReviewRepository = shopReviewRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        return new DashboardResponse(
                userRepository.count(),
                shopRepository.countByStatus(Shop.STATUS_VALIDEE),
                shopRepository.countByStatus(Shop.STATUS_EN_ATTENTE),
                productRepository.countByStatus(Product.STATUS_EN_ATTENTE),
                orderRepository.countByStatus("EN_ATTENTE_PAIEMENT"),
                payoutRepository.countByStatus(VendorPayout.STATUS_ECHEC),
                productReviewRepository.countByStatus(ProductReview.STATUS_VISIBLE)
                        + shopReviewRepository.countByStatus(ShopReview.STATUS_VISIBLE));
    }

    @Transactional(readOnly = true)
    public StatsResponse getStats() {
        Map<String, Long> ordersByStatus = new LinkedHashMap<>();
        for (Object[] row : orderRepository.countGroupByStatus()) {
            ordersByStatus.put((String) row[0], (Long) row[1]);
        }

        List<ShopRevenueResponse> topShops = orderRepository.topShopsByRevenue(PageRequest.of(0, 5)).stream()
                .map(row -> new ShopRevenueResponse((String) row[0], (java.math.BigDecimal) row[1]))
                .toList();

        return new StatsResponse(
                orderRepository.sumRevenue(),
                orderRepository.sumCommission(),
                shopRepository.countByStatus(Shop.STATUS_VALIDEE),
                ordersByStatus,
                topShops);
    }
}
