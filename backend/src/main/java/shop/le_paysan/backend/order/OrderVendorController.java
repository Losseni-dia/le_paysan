package shop.le_paysan.backend.order;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.common.FileStorageService;
import shop.le_paysan.backend.delivery.DeliveryRequestService;
import shop.le_paysan.backend.delivery.dto.BroadcastDeliveryRequest;
import shop.le_paysan.backend.delivery.dto.DeliveryRequestResponse;
import shop.le_paysan.backend.delivery.dto.EligibleLivreurResponse;
import shop.le_paysan.backend.order.dto.AdminOrderResponse;
import shop.le_paysan.backend.shop.Shop;
import shop.le_paysan.backend.shop.ShopRepository;

@RestController
@RequestMapping("/api/v1/vendor/orders")
@PreAuthorize("hasRole('VENDEUR')")
public class OrderVendorController {

    private final OrderService service;
    private final ShopRepository shopRepository;
    private final FileStorageService fileStorageService;
    private final DeliveryRequestService deliveryRequestService;

    public OrderVendorController(
            OrderService service,
            ShopRepository shopRepository,
            FileStorageService fileStorageService,
            DeliveryRequestService deliveryRequestService) {
        this.service = service;
        this.shopRepository = shopRepository;
        this.fileStorageService = fileStorageService;
        this.deliveryRequestService = deliveryRequestService;
    }

    @GetMapping
    public Page<AdminOrderResponse> list(
            @AuthenticationPrincipal SecurityUser principal,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return service.listForVendor(shopOf(principal), status, pageable);
    }

    @GetMapping("/{id}")
    public AdminOrderResponse detail(@AuthenticationPrincipal SecurityUser principal, @PathVariable Long id) {
        return service.getForVendor(shopOf(principal), id);
    }

    /**
     * Cf. remarque utilisateur (plan "timeline de commande" Phase 2) : le vendeur peut lui-meme
     * livrer (pas de livreur assigne) et doit pouvoir faire signer/photographier la remise, comme
     * le livreur. La preuve reste optionnelle : "Livraison effectuee" doit toujours fonctionner
     * meme sans preuve (filet de securite).
     */
    @PatchMapping(value = "/{id}/status", consumes = "multipart/form-data")
    public AdminOrderResponse markNextStatus(
            @AuthenticationPrincipal SecurityUser principal,
            @PathVariable Long id,
            @RequestParam(required = false) String proofType,
            @RequestParam(required = false) MultipartFile proof) {
        String proofUrl = null;
        if (proof != null && !proof.isEmpty()) {
            if (!OrderStatusHistory.PROOF_SIGNATURE.equals(proofType) && !OrderStatusHistory.PROOF_PHOTO.equals(proofType)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type de preuve invalide");
            }
            proofUrl = fileStorageService.store(proof, "delivery-proofs");
        } else {
            proofType = OrderStatusHistory.PROOF_NONE;
        }
        return service.markNextStatusForVendor(shopOf(principal), id, proofType, proofUrl);
    }

    /** Livreurs disponibles a proximite de la livraison de cette commande (cf. diffusion). */
    @GetMapping("/{id}/livreurs-eligibles")
    public List<EligibleLivreurResponse> eligibleLivreurs(
            @AuthenticationPrincipal SecurityUser principal, @PathVariable Long id) {
        return deliveryRequestService.listEligibleLivreurs(shopOf(principal), id);
    }

    /** Diffuse la demande de livraison a plusieurs livreurs : le premier a accepter l'emporte. */
    @PostMapping("/{id}/diffuser")
    public List<DeliveryRequestResponse> broadcast(
            @AuthenticationPrincipal SecurityUser principal,
            @PathVariable Long id,
            @Valid @RequestBody BroadcastDeliveryRequest request) {
        return deliveryRequestService.broadcast(shopOf(principal), id, request.livreurIds());
    }

    private Shop shopOf(SecurityUser principal) {
        return shopRepository
                .findByOwnerId(principal.getUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune boutique"));
    }
}
