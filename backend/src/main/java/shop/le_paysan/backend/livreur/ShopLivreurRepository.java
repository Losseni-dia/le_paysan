package shop.le_paysan.backend.livreur;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopLivreurRepository extends JpaRepository<ShopLivreur, Long> {

    List<ShopLivreur> findByShopIdOrderByCreatedAtDesc(Long shopId);

    Optional<ShopLivreur> findByShopIdAndLivreurId(Long shopId, Long livreurId);

    Optional<ShopLivreur> findByLivreurId(Long livreurUserId);

    @Query("SELECT sl FROM ShopLivreur sl WHERE (:status IS NULL OR sl.livreur.kycStatus = :status) ORDER BY sl.createdAt DESC")
    Page<ShopLivreur> findAllForAdmin(@Param("status") String status, Pageable pageable);
}
