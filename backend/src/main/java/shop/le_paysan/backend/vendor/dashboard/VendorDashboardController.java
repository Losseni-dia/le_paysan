package shop.le_paysan.backend.vendor.dashboard;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.shop.Shop;
import shop.le_paysan.backend.shop.ShopRepository;
import shop.le_paysan.backend.vendor.dashboard.dto.VendorDashboardResponse;
import shop.le_paysan.backend.vendor.dashboard.dto.VendorStatsResponse;

@RestController
@RequestMapping("/api/v1/vendor")
@PreAuthorize("hasRole('VENDEUR')")
public class VendorDashboardController {

    private final VendorDashboardService service;
    private final ShopRepository shopRepository;

    public VendorDashboardController(VendorDashboardService service, ShopRepository shopRepository) {
        this.service = service;
        this.shopRepository = shopRepository;
    }

    @GetMapping("/dashboard")
    public VendorDashboardResponse dashboard(@AuthenticationPrincipal SecurityUser principal) {
        return service.getDashboard(shopOf(principal));
    }

    @GetMapping("/statistiques")
    public VendorStatsResponse stats(@AuthenticationPrincipal SecurityUser principal) {
        return service.getStats(shopOf(principal));
    }

    private Shop shopOf(SecurityUser principal) {
        return shopRepository
                .findByOwnerId(principal.getUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune boutique"));
    }
}
