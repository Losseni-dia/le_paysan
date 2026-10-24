package shop.le_paysan.backend.delivery;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.delivery.dto.DeliveryRequestResponse;

@RestController
@RequestMapping("/api/v1/livreur/demandes")
@PreAuthorize("hasRole('LIVREUR')")
public class DeliveryRequestLivreurController {

    private final DeliveryRequestService service;

    public DeliveryRequestLivreurController(DeliveryRequestService service) {
        this.service = service;
    }

    @GetMapping
    public List<DeliveryRequestResponse> list(@AuthenticationPrincipal SecurityUser principal) {
        return service.listForLivreur(principal.getUser());
    }

    @PostMapping("/{id}/accepter")
    public DeliveryRequestResponse accept(@AuthenticationPrincipal SecurityUser principal, @PathVariable Long id) {
        return service.accept(principal.getUser(), id);
    }
}
