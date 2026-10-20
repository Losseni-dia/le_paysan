package shop.le_paysan.backend.livreur;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.livreur.dto.LivreurAdminResponse;
import shop.le_paysan.backend.livreur.dto.RejectLivreurKycRequest;

@RestController
@RequestMapping("/api/admin/livreurs")
@PreAuthorize("hasRole('ADMIN')")
public class LivreurAdminController {

    private final LivreurService service;

    public LivreurAdminController(LivreurService service) {
        this.service = service;
    }

    @GetMapping
    public Page<LivreurAdminResponse> list(@RequestParam(required = false) String kycStatus, Pageable pageable) {
        return service.listForAdmin(kycStatus, pageable);
    }

    @GetMapping("/{id}")
    public LivreurAdminResponse detail(@PathVariable Long id) {
        return service.getAdminDetail(id);
    }

    @PostMapping("/{id}/valider-kyc")
    public LivreurAdminResponse validateKyc(@PathVariable Long id, @AuthenticationPrincipal SecurityUser admin) {
        return service.validateKyc(id, admin.getUser());
    }

    @PostMapping("/{id}/rejeter-kyc")
    public LivreurAdminResponse rejectKyc(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser admin,
            @Valid @RequestBody RejectLivreurKycRequest request) {
        return service.rejectKyc(id, admin.getUser(), request.reason());
    }
}
