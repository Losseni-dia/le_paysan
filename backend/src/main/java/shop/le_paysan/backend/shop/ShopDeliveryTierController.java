package shop.le_paysan.backend.shop;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.shop.dto.ShopDeliveryCityRequest;
import shop.le_paysan.backend.shop.dto.ShopDeliveryCityResponse;
import shop.le_paysan.backend.shop.dto.ShopDeliveryTierRequest;
import shop.le_paysan.backend.shop.dto.ShopDeliveryTierResponse;

@RestController
public class ShopDeliveryTierController {

    private final ShopDeliveryTierService tierService;
    private final ShopRepository shopRepository;

    public ShopDeliveryTierController(ShopDeliveryTierService tierService, ShopRepository shopRepository) {
        this.tierService = tierService;
        this.shopRepository = shopRepository;
    }

    /** Public : affiche le bareme de livraison d'une boutique sur la fiche produit/boutique. */
    @GetMapping("/api/shops/{id}/paliers-livraison")
    public List<ShopDeliveryTierResponse> publicTiers(@PathVariable Long id) {
        return tierService.list(id);
    }

    @GetMapping("/api/vendor/shop/paliers-livraison")
    @PreAuthorize("hasRole('VENDEUR')")
    public List<ShopDeliveryTierResponse> myTiers(@AuthenticationPrincipal SecurityUser principal) {
        return tierService.list(shopOf(principal).getId());
    }

    @PutMapping("/api/vendor/shop/paliers-livraison")
    @PreAuthorize("hasRole('VENDEUR')")
    public List<ShopDeliveryTierResponse> replaceMyTiers(
            @AuthenticationPrincipal SecurityUser principal, @Valid @RequestBody List<ShopDeliveryTierRequest> tiers) {
        return tierService.replace(shopOf(principal), tiers);
    }

    /** Public : villes hors rayon habituel ou la boutique livre explicitement, a prix fixe. */
    @GetMapping("/api/shops/{id}/villes-livraison")
    public List<ShopDeliveryCityResponse> publicCities(@PathVariable Long id) {
        return tierService.listCities(id);
    }

    @GetMapping("/api/vendor/shop/villes-livraison")
    @PreAuthorize("hasRole('VENDEUR')")
    public List<ShopDeliveryCityResponse> myCities(@AuthenticationPrincipal SecurityUser principal) {
        return tierService.listCities(shopOf(principal).getId());
    }

    @PutMapping("/api/vendor/shop/villes-livraison")
    @PreAuthorize("hasRole('VENDEUR')")
    public List<ShopDeliveryCityResponse> replaceMyCities(
            @AuthenticationPrincipal SecurityUser principal, @Valid @RequestBody List<ShopDeliveryCityRequest> cities) {
        return tierService.replaceCities(shopOf(principal), cities);
    }

    private Shop shopOf(SecurityUser principal) {
        return shopRepository
                .findByOwnerId(principal.getUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune boutique"));
    }
}
