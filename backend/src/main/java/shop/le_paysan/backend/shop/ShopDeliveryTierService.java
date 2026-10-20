package shop.le_paysan.backend.shop;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.common.GeoUtils;
import shop.le_paysan.backend.shop.dto.ShopDeliveryCityRequest;
import shop.le_paysan.backend.shop.dto.ShopDeliveryCityResponse;
import shop.le_paysan.backend.shop.dto.ShopDeliveryTierRequest;
import shop.le_paysan.backend.shop.dto.ShopDeliveryTierResponse;

@Service
public class ShopDeliveryTierService {

    private final ShopDeliveryTierRepository repository;
    private final ShopDeliveryCityRepository cityRepository;

    public ShopDeliveryTierService(ShopDeliveryTierRepository repository, ShopDeliveryCityRepository cityRepository) {
        this.repository = repository;
        this.cityRepository = cityRepository;
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

    @Transactional(readOnly = true)
    public List<ShopDeliveryCityResponse> listCities(Long shopId) {
        return cityRepository.findByShopIdOrderByCityNameAsc(shopId).stream()
                .map(ShopDeliveryCityResponse::from)
                .toList();
    }

    /** Cf. demande utilisateur : villes de livraison hors zone habituelle, declarees explicitement par le vendeur. */
    @Transactional
    public List<ShopDeliveryCityResponse> replaceCities(Shop shop, List<ShopDeliveryCityRequest> cities) {
        Set<String> seen = new java.util.HashSet<>();
        for (ShopDeliveryCityRequest c : cities) {
            if (!seen.add(c.cityName().trim().toLowerCase())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une ville ne peut être déclarée qu'une seule fois");
            }
        }

        cityRepository.deleteByShopId(shop.getId());
        List<ShopDeliveryCity> saved = cities.stream()
                .map(c -> {
                    ShopDeliveryCity city = new ShopDeliveryCity();
                    city.setShop(shop);
                    city.setCityName(c.cityName().trim());
                    city.setPrice(c.price());
                    return cityRepository.save(city);
                })
                .toList();
        return saved.stream().map(ShopDeliveryCityResponse::from).toList();
    }

    /** Cf. visibilite catalogue : shop ids couvrant explicitement cette ville, meme hors rayon de distance. */
    @Transactional(readOnly = true)
    public Set<Long> shopIdsCoveringCity(String cityName) {
        if (cityName == null || cityName.isBlank()) {
            return Set.of();
        }
        return cityRepository.findByCityNameIgnoreCase(cityName).stream()
                .map(c -> c.getShop().getId())
                .collect(Collectors.toSet());
    }

    /**
     * Calcule le prix de livraison a domicile : priorite a une ville declaree explicitement par le
     * vendeur (prix fixe, independant de la distance), sinon prix du palier de distance couvrant
     * cette distance + supplement produit (poids/volume). Retourne empty si la distance depasse
     * tous les paliers configures ET qu'aucune ville explicite ne correspond (livraison non
     * proposee). Si la boutique n'a rien configure du tout, on reste compatible avec l'ancien
     * comportement (supplement seul).
     */
    @Transactional(readOnly = true)
    public Optional<BigDecimal> computeDeliveryPrice(Shop shop, BigDecimal productSupplement, String cityName, double distanceKm) {
        BigDecimal supplement = productSupplement == null ? BigDecimal.ZERO : productSupplement;

        if (cityName != null && !cityName.isBlank()) {
            Optional<ShopDeliveryCity> cityMatch = cityRepository.findByShopIdAndCityNameIgnoreCase(shop.getId(), cityName);
            if (cityMatch.isPresent()) {
                return Optional.of(cityMatch.get().getPrice().add(supplement));
            }
        }

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
