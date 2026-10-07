package shop.le_paysan.backend.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /** Cf. docs/02 UC-11 : avis vérifié achat — dernier item livré de ce produit pour cet acheteur. */
    Optional<OrderItem> findFirstByProduct_IdAndOrder_BuyerUser_IdAndOrder_StatusOrderByOrder_CreatedAtDesc(
            Long productId, Long buyerUserId, String orderStatus);

    @Query("""
            SELECT oi.productNameSnapshot, SUM(oi.quantity), SUM(oi.unitPriceSnapshot * oi.quantity)
            FROM OrderItem oi
            WHERE oi.order.shop.id = :shopId AND oi.order.status NOT IN ('EN_ATTENTE_PAIEMENT', 'ANNULEE')
            GROUP BY oi.productNameSnapshot
            ORDER BY SUM(oi.unitPriceSnapshot * oi.quantity) DESC
            """)
    List<Object[]> topProductsForShop(@Param("shopId") Long shopId, Pageable pageable);
}
