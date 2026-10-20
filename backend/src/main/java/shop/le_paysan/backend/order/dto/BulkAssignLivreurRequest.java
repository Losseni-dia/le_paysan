package shop.le_paysan.backend.order.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record BulkAssignLivreurRequest(@NotEmpty List<Long> orderIds, @NotNull Long livreurId) {
}
