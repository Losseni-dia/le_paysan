package shop.le_paysan.backend.auth;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import shop.le_paysan.backend.auth.dto.AccountRecoveryResponse;

@RestController
public class AccountRecoveryController {

    private final AccountRecoveryService service;

    public AccountRecoveryController(AccountRecoveryService service) {
        this.service = service;
    }

    @PostMapping(value = "/api/v1/auth/account-recovery-requests", consumes = "multipart/form-data")
    public AccountRecoveryResponse create(
            @RequestParam String username, @RequestParam("selfie") MultipartFile selfie) {
        return service.create(username, selfie);
    }

    @GetMapping("/api/v1/admin/account-recovery-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AccountRecoveryResponse> list(
            @RequestParam(required = false) String status, Pageable pageable) {
        return service.list(status, pageable);
    }

    @GetMapping("/api/v1/admin/account-recovery-requests/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AccountRecoveryResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    /** @return le mot de passe temporaire a communiquer manuellement au user (hors-ligne). */
    @PostMapping("/api/v1/admin/account-recovery-requests/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public java.util.Map<String, String> approve(
            @PathVariable Long id, @AuthenticationPrincipal SecurityUser admin) {
        String temporaryPassword = service.approve(id, admin.getUser());
        return java.util.Map.of("temporaryPassword", temporaryPassword);
    }

    @PostMapping("/api/v1/admin/account-recovery-requests/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public void reject(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser admin,
            @RequestParam(required = false) String reason) {
        service.reject(id, admin.getUser(), reason);
    }
}
