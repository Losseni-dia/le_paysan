package shop.le_paysan.backend.livreur;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopLivreurRepository extends JpaRepository<ShopLivreur, Long> {

    List<ShopLivreur> findByShopIdOrderByCreatedAtDesc(Long shopId);

    Optional<ShopLivreur> findByShopIdAndLivreurId(Long shopId, Long livreurId);
}
