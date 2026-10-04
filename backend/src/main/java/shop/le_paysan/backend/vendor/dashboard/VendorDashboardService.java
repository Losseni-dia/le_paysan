package shop.le_paysan.backend.vendor.dashboard;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import shop.le_paysan.backend.order.Order;
import shop.le_paysan.backend.order.OrderItemRepository;
import shop.le_paysan.backend.order.OrderRepository;
import shop.le_paysan.backend.product.Product;
import shop.le_paysan.backend.product.ProductRepository;
import shop.le_paysan.backend.shop.Shop;
import shop.le_paysan.backend.vendor.dashboard.dto.ProductSalesResponse;
import shop.le_paysan.backend.vendor.dashboard.dto.VendorDashboardResponse;
import shop.le_paysan.backend.vendor.dashboard.dto.VendorStatsResponse;

@Service
public class VendorDashboardService {

    private static final List<String> STATUSES_A_TRAITER = List.of(Order.STATUS_PAYEE, Order.STATUS_EN_PREPARATION);

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public VendorDashboardService(
            ProductRepository productRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public VendorDashboardResponse getDashboard(Shop shop) {
        return new VendorDashboardResponse(
                productRepository.countByShopIdAndStatus(shop.getId(), Product.STATUS_VALIDE),
                productRepository.countByShopIdAndStatus(shop.getId(), Product.STATUS_EN_ATTENTE),
                orderRepository.countByShopIdAndStatusIn(shop.getId(), STATUSES_A_TRAITER),
                orderRepository.sumNetRevenueForShop(shop.getId()));
    }

    @Transactional(readOnly = true)
    public VendorStatsResponse getStats(Shop shop) {
        Map<String, Long> ordersByStatus = new LinkedHashMap<>();
        for (Object[] row : orderRepository.countGroupByStatusForShop(shop.getId())) {
            ordersByStatus.put((String) row[0], (Long) row[1]);
        }

        List<ProductSalesResponse> topProducts = orderItemRepository.topProductsForShop(shop.getId(), PageRequest.of(0, 5)).stream()
                .map(row -> new ProductSalesResponse((String) row[0], (Long) row[1], (BigDecimal) row[2]))
                .toList();

        return new VendorStatsResponse(orderRepository.sumNetRevenueForShop(shop.getId()), ordersByStatus, topProducts);
    }
}
