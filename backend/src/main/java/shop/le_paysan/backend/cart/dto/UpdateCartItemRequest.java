package shop.le_paysan.backend.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequest(@NotNull @Min(1) Integer quantity, @NotNull String deliveryMode, String deliveryAddress) {
}
