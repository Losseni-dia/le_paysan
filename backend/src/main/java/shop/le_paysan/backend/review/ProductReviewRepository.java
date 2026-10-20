package shop.le_paysan.backend.review;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    Optional<ProductReview> findByProductIdAndBuyerUserId(Long productId, Long buyerUserId);

    Page<ProductReview> findByProductIdAndStatusOrderByCreatedAtDesc(Long productId, String status, Pageable pageable);

    Page<ProductReview> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId AND r.status = 'VISIBLE'")
    Double averageRating(@Param("productId") Long productId);

    long countByProductIdAndStatus(Long productId, String status);

    long countByStatus(String status);

    @Query(
            "SELECT r.product.id, AVG(r.rating), COUNT(r) FROM ProductReview r "
                    + "WHERE r.product.id IN :productIds AND r.status = 'VISIBLE' GROUP BY r.product.id")
    List<Object[]> ratingSummariesFor(@Param("productIds") List<Long> productIds);
}
