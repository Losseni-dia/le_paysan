package shop.le_paysan.backend.shop;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.common.GeoUtils;
import shop.le_paysan.backend.shop.dto.ShopDeliveryTierRequest;
import shop.le_paysan.backend.shop.dto.ShopDeliveryTierResponse;

@Service
public class ShopDeliveryTierService {

    private final ShopDeliveryTierRepository repository;

    public ShopDeliveryTierService(ShopDeliveryTierRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ShopDeliveryTierResponse> list(Long shopId) {
        return repository.findByShopIdOrderByMaxDistanceKmAsc(shopId).stream()
                .map(ShopDeliveryTierResponse::from)
                .toList();
    }

    /** Remplace l'ensemble des paliers de la boutique en une fois (le vendeur soumet toute la liste). */
    @Transactional
    public List<ShopDeliveryTierResponse> replace(Shop shop, List<ShopDeliveryTierRequest> tiers) {
        List<ShopDeliveryTierRequest> sorted =
                tiers.stream().sorted(Comparator.comparingInt(ShopDeliveryTierRequest::maxDistanceKm)).toList();
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).maxDistanceKm().equals(sorted.get(i - 1).maxDistanceKm())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deux paliers ne peuvent pas avoir la même distance maximale");
            }
        }

        repository.deleteByShopId(shop.getId());
        List<ShopDeliveryTier> saved = sorted.stream()
                .map(t -> {
                    ShopDeliveryTier tier = new ShopDeliveryTier();
                    tier.setShop(shop);
                    tier.setMaxDistanceKm(t.maxDistanceKm());
                    tier.setPrice(t.price());
                    return repository.save(tier);
                })
                .toList();
        return saved.stream().map(ShopDeliveryTierResponse::from).toList();
    }

    /**
     * Calcule le prix de livraison a domicile pour une distance donnee : prix du palier couvrant
     * cette distance + supplement produit (poids/volume). Retourne empty si la distance depasse
     * tous les paliers configures (livraison non proposee a cette distance). Si la boutique n'a
     * configure aucun palier, on reste compatible avec l'ancien comportement (supplement seul).
     */
    @Transactional(readOnly = true)
    public Optional<BigDecimal> computeDeliveryPrice(Shop shop, BigDecimal productSupplement, double distanceKm) {
        BigDecimal supplement = productSupplement == null ? BigDecimal.ZERO : productSupplement;
        List<ShopDeliveryTier> tiers = repository.findByShopIdOrderByMaxDistanceKmAsc(shop.getId());
        if (tiers.isEmpty()) {
            return Optional.of(supplement);
        }
        return tiers.stream()
                .filter(t -> distanceKm <= t.getMaxDistanceKm())
                .findFirst()
                .map(t -> t.getPrice().add(supplement));
    }

    public double distanceKm(Shop shop, double buyerLat, double buyerLng) {
        return GeoUtils.haversineKm(
                shop.getLatitude().doubleValue(), shop.getLongitude().doubleValue(), buyerLat, buyerLng);
    }
}
