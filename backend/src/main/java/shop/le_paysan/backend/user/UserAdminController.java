package shop.le_paysan.backend.user;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.user.dto.AdminUserResponse;
import shop.le_paysan.backend.user.dto.UpdateUserRolesRequest;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserAdminService service;

    public UserAdminController(UserAdminService service) {
        this.service = service;
    }

    @GetMapping
    public Page<AdminUserResponse> list(
            @RequestParam(required = false) String search, @RequestParam(required = false) String role, Pageable pageable) {
        return service.list(search, role, pageable);
    }

    @PatchMapping("/{id}/enabled")
    public AdminUserResponse setEnabled(
            @PathVariable Long id, @RequestBody Map<String, Boolean> body, @AuthenticationPrincipal SecurityUser admin) {
        return service.setEnabled(id, Boolean.TRUE.equals(body.get("enabled")), admin.getUser());
    }

    @PatchMapping("/{id}/roles")
    public AdminUserResponse setRoles(
            @PathVariable Long id, @Valid @RequestBody UpdateUserRolesRequest request, @AuthenticationPrincipal SecurityUser admin) {
        return service.setRoles(id, request.roles(), admin.getUser());
    }
}
