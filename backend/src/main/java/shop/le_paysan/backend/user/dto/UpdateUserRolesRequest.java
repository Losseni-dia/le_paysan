package shop.le_paysan.backend.user.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record UpdateUserRolesRequest(@NotEmpty List<String> roles) {
}
