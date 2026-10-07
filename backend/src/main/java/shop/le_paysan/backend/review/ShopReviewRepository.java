package shop.le_paysan.backend.review;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopReviewRepository extends JpaRepository<ShopReview, Long> {

    Optional<ShopReview> findByShopIdAndBuyerUserId(Long shopId, Long buyerUserId);

    Page<ShopReview> findByShopIdAndStatusOrderByCreatedAtDesc(Long shopId, String status, Pageable pageable);

    Page<ShopReview> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM ShopReview r WHERE r.shop.id = :shopId AND r.status = 'VISIBLE'")
    Double averageRating(@Param("shopId") Long shopId);

    long countByShopIdAndStatus(Long shopId, String status);

    long countByStatus(String status);
}
