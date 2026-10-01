package shop.le_paysan.backend.user.dto;

import java.util.List;

import shop.le_paysan.backend.user.Role;
import shop.le_paysan.backend.user.User;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phone,
        boolean hasSecuritySelfie,
        List<String> roles) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getSecuritySelfieUrl() != null,
                user.getRoles().stream().map(Role::getName).toList());
    }
}
