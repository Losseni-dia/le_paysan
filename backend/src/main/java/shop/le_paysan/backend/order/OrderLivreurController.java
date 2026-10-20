package shop.le_paysan.backend.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.common.FileStorageService;
import shop.le_paysan.backend.order.dto.AdminOrderResponse;

@RestController
@RequestMapping("/api/livreur/orders")
@PreAuthorize("hasRole('LIVREUR')")
public class OrderLivreurController {

    private final OrderService service;
    private final FileStorageService fileStorageService;

    public OrderLivreurController(OrderService service, FileStorageService fileStorageService) {
        this.service = service;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public Page<AdminOrderResponse> list(
            @AuthenticationPrincipal SecurityUser principal,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return service.listForLivreur(principal.getUser(), status, pageable);
    }

    @GetMapping("/{id}")
    public AdminOrderResponse detail(@AuthenticationPrincipal SecurityUser principal, @PathVariable Long id) {
        return service.getForLivreur(principal.getUser(), id);
    }

    @PostMapping("/{id}/recuperer")
    public AdminOrderResponse markPickedUp(@AuthenticationPrincipal SecurityUser principal, @PathVariable Long id) {
        return service.markPickedUpByLivreur(principal.getUser(), id);
    }

    @PostMapping("/{id}/en-route")
    public AdminOrderResponse markEnRoute(@AuthenticationPrincipal SecurityUser principal, @PathVariable Long id) {
        return service.markEnRouteByLivreur(principal.getUser(), id);
    }

    @PostMapping(value = "/{id}/confirmer-remise", consumes = "multipart/form-data")
    public AdminOrderResponse confirmDelivery(
            @AuthenticationPrincipal SecurityUser principal,
            @PathVariable Long id,
            @RequestParam String proofType,
            @RequestParam MultipartFile proof) {
        if (!OrderStatusHistory.PROOF_SIGNATURE.equals(proofType) && !OrderStatusHistory.PROOF_PHOTO.equals(proofType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type de preuve invalide");
        }
        String proofUrl = fileStorageService.store(proof, "delivery-proofs");
        return service.confirmDeliveryByLivreur(principal.getUser(), id, proofType, proofUrl);
    }
}
