package shop.le_paysan.backend.shop;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopDeliveryCityRepository extends JpaRepository<ShopDeliveryCity, Long> {

    List<ShopDeliveryCity> findByShopIdOrderByCityNameAsc(Long shopId);

    Optional<ShopDeliveryCity> findByShopIdAndCityNameIgnoreCase(Long shopId, String cityName);

    /** Cf. visibilite catalogue : tous les shop_id couvrant explicitement cette ville. */
    List<ShopDeliveryCity> findByCityNameIgnoreCase(String cityName);

    void deleteByShopId(Long shopId);
}
