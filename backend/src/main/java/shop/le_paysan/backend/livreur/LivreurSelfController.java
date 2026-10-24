package shop.le_paysan.backend.livreur;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.livreur.dto.LivreurResponse;
import shop.le_paysan.backend.livreur.dto.SetAvailabilityRequest;

/** Le livreur gere sa propre disponibilite, visible par le vendeur dans sa liste de livreurs. */
@RestController
@RequestMapping("/api/v1/livreur")
@PreAuthorize("hasRole('LIVREUR')")
public class LivreurSelfController {

    private final LivreurService service;

    public LivreurSelfController(LivreurService service) {
        this.service = service;
    }

    @GetMapping("/moi")
    public LivreurResponse me(@AuthenticationPrincipal SecurityUser principal) {
        return service.getSelf(principal.getUser());
    }

    @PatchMapping("/disponibilite")
    public LivreurResponse setAvailability(
            @AuthenticationPrincipal SecurityUser principal, @RequestBody SetAvailabilityRequest request) {
        return service.setAvailability(principal.getUser(), request.available());
    }

    /** Soumission/remplacement des documents KYC par le livreur lui-meme (inscription volontaire). */
    @PutMapping(value = "/documents", consumes = "multipart/form-data")
    public LivreurResponse uploadDocuments(
            @AuthenticationPrincipal SecurityUser principal,
            @RequestParam(required = false) MultipartFile photo,
            @RequestParam(required = false) MultipartFile idDocumentFront,
            @RequestParam(required = false) MultipartFile idDocumentBack,
            @RequestParam(required = false) MultipartFile selfieWithId) {
        return service.uploadDocuments(principal.getUser(), photo, idDocumentFront, idDocumentBack, selfieWithId);
    }
}
