package shop.le_paysan.backend.delivery.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record BroadcastDeliveryRequest(@NotEmpty List<Long> livreurIds) {
}
