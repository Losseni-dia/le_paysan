package shop.le_paysan.backend.user;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.user.dto.AdminUserResponse;

@Service
public class UserAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserAdminService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> list(String search, String role, Pageable pageable) {
        return userRepository.search(search, role, pageable).map(AdminUserResponse::from);
    }

    @Transactional
    public AdminUserResponse setEnabled(Long userId, boolean enabled, User currentAdmin) {
        User user = findOrThrow(userId);
        if (user.getId().equals(currentAdmin.getId()) && !enabled) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas désactiver votre propre compte");
        }
        user.setEnabled(enabled);
        user.setUpdatedAt(LocalDateTime.now());
        return AdminUserResponse.from(userRepository.save(user));
    }

    @Transactional
    public AdminUserResponse setRoles(Long userId, List<String> roleNames, User currentAdmin) {
        User user = findOrThrow(userId);
        if (user.getId().equals(currentAdmin.getId()) && !roleNames.contains(Role.ADMIN)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Vous ne pouvez pas retirer votre propre rôle administrateur");
        }

        Set<Role> roles = new LinkedHashSet<>();
        for (String roleName : roleNames) {
            roles.add(roleRepository
                    .findByName(roleName)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rôle inconnu : " + roleName)));
        }
        user.setRoles(roles);
        user.setUpdatedAt(LocalDateTime.now());
        return AdminUserResponse.from(userRepository.save(user));
    }

    private User findOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
