package shop.le_paysan.backend.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByOrderNumber(String orderNumber);

    Optional<Order> findByIdAndBuyerUserId(Long id, Long buyerUserId);

    List<Order> findByBuyerUserIdOrderByCreatedAtDesc(Long buyerUserId);

    List<Order> findByShopIdOrderByCreatedAtDesc(Long shopId);

    List<Order> findByPaymentTransactionId(Long paymentTransactionId);

    List<Order> findByIdInAndBuyerUserId(List<Long> ids, Long buyerUserId);

    Page<Order> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Order> findByShopIdOrderByCreatedAtDesc(Long shopId, Pageable pageable);

    /** Cf. docs/02 UC-11 : avis vérifié achat — derniere commande livree de cette boutique pour cet acheteur. */
    Optional<Order> findFirstByShopIdAndBuyerUserIdAndStatusOrderByCreatedAtDesc(Long shopId, Long buyerUserId, String status);

    Page<Order> findByShopIdAndStatusOrderByCreatedAtDesc(Long shopId, String status, Pageable pageable);

    List<Order> findByStatusAndCreatedAtBefore(String status, LocalDateTime cutoff);

    List<Order> findByStatusAndUpdatedAtBefore(String status, LocalDateTime cutoff);

    /** Cf. rappel avis 24h apres livraison : commandes livrees depuis le cutoff, pas encore relancees. */
    List<Order> findByStatusAndReviewReminderSentFalseAndUpdatedAtBefore(String status, LocalDateTime cutoff);

    Page<Order> findByAssignedLivreurIdAndStatusOrderByCreatedAtDesc(Long livreurId, String status, Pageable pageable);

    long countByStatus(String status);

    long countByShopIdAndStatusIn(Long shopId, List<String> statuses);

    @Query("SELECT COALESCE(SUM(o.subtotalAmount - o.commissionAmount + o.deliveryAmount), 0) FROM Order o "
            + "WHERE o.shop.id = :shopId AND o.status NOT IN ('EN_ATTENTE_PAIEMENT', 'ANNULEE')")
    BigDecimal sumNetRevenueForShop(@Param("shopId") Long shopId);

    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.shop.id = :shopId GROUP BY o.status")
    List<Object[]> countGroupByStatusForShop(@Param("shopId") Long shopId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status NOT IN ('EN_ATTENTE_PAIEMENT', 'ANNULEE')")
    BigDecimal sumRevenue();

    @Query("SELECT COALESCE(SUM(o.commissionAmount), 0) FROM Order o WHERE o.status NOT IN ('EN_ATTENTE_PAIEMENT', 'ANNULEE')")
    BigDecimal sumCommission();

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countGroupByStatus();

    @Query("""
            SELECT o.shop.name, SUM(o.totalAmount) FROM Order o
            WHERE o.status NOT IN ('EN_ATTENTE_PAIEMENT', 'ANNULEE')
            GROUP BY o.shop.name
            ORDER BY SUM(o.totalAmount) DESC
            """)
    List<Object[]> topShopsByRevenue(Pageable pageable);
}
