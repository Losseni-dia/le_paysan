package shop.le_paysan.backend.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByOrderNumber(String orderNumber);

    List<Order> findByBuyerUserIdOrderByCreatedAtDesc(Long buyerUserId);

    List<Order> findByShopIdOrderByCreatedAtDesc(Long shopId);
}
