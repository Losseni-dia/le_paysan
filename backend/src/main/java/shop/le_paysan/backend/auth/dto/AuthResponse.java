package shop.le_paysan.backend.auth.dto;

import java.util.List;

import shop.le_paysan.backend.user.dto.UserResponse;

public record AuthResponse(String accessToken, String refreshToken, UserResponse user, List<String> roles) {
}
