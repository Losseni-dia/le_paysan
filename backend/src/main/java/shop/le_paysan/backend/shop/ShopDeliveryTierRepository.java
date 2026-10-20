package shop.le_paysan.backend.shop;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopDeliveryTierRepository extends JpaRepository<ShopDeliveryTier, Long> {

    List<ShopDeliveryTier> findByShopIdOrderByMaxDistanceKmAsc(Long shopId);

    void deleteByShopId(Long shopId);
}
