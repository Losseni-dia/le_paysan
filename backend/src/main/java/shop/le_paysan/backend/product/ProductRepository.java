package shop.le_paysan.backend.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /** Verrou pessimiste : serialise les decrementations de stock concurrentes sur un meme
     * produit (cf. audit securite, confirmation de paiement). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Product> findByShopId(Long shopId, Pageable pageable);

    Page<Product> findByStatus(String status, Pageable pageable);

    List<Product> findByShopIdAndStatus(Long shopId, String status);

    long countByStatus(String status);

    long countByShopIdAndStatus(Long shopId, String status);

    boolean existsByCategoryId(Long categoryId);
}
