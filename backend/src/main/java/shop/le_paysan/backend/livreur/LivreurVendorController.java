package shop.le_paysan.backend.livreur;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.livreur.dto.CreateLivreurRequest;
import shop.le_paysan.backend.livreur.dto.LivreurResponse;
import shop.le_paysan.backend.shop.Shop;
import shop.le_paysan.backend.shop.ShopRepository;

@RestController
@RequestMapping("/api/vendor/livreurs")
@PreAuthorize("hasRole('VENDEUR')")
public class LivreurVendorController {

    private final LivreurService service;
    private final ShopRepository shopRepository;

    public LivreurVendorController(LivreurService service, ShopRepository shopRepository) {
        this.service = service;
        this.shopRepository = shopRepository;
    }

    @GetMapping
    public List<LivreurResponse> list(@AuthenticationPrincipal SecurityUser principal) {
        return service.listForShop(shopOf(principal));
    }

    @PostMapping
    public LivreurResponse create(
            @AuthenticationPrincipal SecurityUser principal, @Valid @RequestBody CreateLivreurRequest request) {
        return service.create(shopOf(principal), request);
    }

    private Shop shopOf(SecurityUser principal) {
        return shopRepository
                .findByOwnerId(principal.getUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune boutique"));
    }
}
