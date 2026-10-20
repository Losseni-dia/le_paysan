package shop.le_paysan.backend.livreur;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.livreur.dto.CreateLivreurRequest;
import shop.le_paysan.backend.livreur.dto.LivreurResponse;
import shop.le_paysan.backend.livreur.dto.ResetLivreurPasswordRequest;
import shop.le_paysan.backend.livreur.dto.SetLivreurEnabledRequest;
import shop.le_paysan.backend.livreur.dto.UpdateLivreurRequest;
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

    @PostMapping(consumes = "multipart/form-data")
    public LivreurResponse create(
            @AuthenticationPrincipal SecurityUser principal, @ModelAttribute CreateLivreurRequest request) {
        return service.create(shopOf(principal), request);
    }

    @PatchMapping(value = "/{id}", consumes = "multipart/form-data")
    public LivreurResponse update(
            @AuthenticationPrincipal SecurityUser principal,
            @PathVariable Long id,
            @ModelAttribute UpdateLivreurRequest request) {
        return service.update(shopOf(principal), id, request);
    }

    @PatchMapping("/{id}/enabled")
    public LivreurResponse setEnabled(
            @AuthenticationPrincipal SecurityUser principal,
            @PathVariable Long id,
            @RequestBody SetLivreurEnabledRequest request) {
        return service.setEnabled(shopOf(principal), id, request.enabled());
    }

    @PatchMapping("/{id}/mot-de-passe")
    public LivreurResponse resetPassword(
            @AuthenticationPrincipal SecurityUser principal,
            @PathVariable Long id,
            @Valid @RequestBody ResetLivreurPasswordRequest request) {
        return service.resetPassword(shopOf(principal), id, request.password());
    }

    private Shop shopOf(SecurityUser principal) {
        return shopRepository
                .findByOwnerId(principal.getUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune boutique"));
    }
}
