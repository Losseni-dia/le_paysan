package shop.le_paysan.backend.user.dto;

import java.time.LocalDateTime;
import java.util.List;

import shop.le_paysan.backend.user.Role;
import shop.le_paysan.backend.user.User;

public record AdminUserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phone,
        boolean enabled,
        List<String> roles,
        LocalDateTime createdAt) {

    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.isEnabled(),
                user.getRoles().stream().map(Role::getName).sorted().toList(),
                user.getCreatedAt());
    }
}
