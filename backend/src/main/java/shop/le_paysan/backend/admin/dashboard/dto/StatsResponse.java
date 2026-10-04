package shop.le_paysan.backend.admin.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record StatsResponse(
        BigDecimal chiffreAffairesTotal,
        BigDecimal commissionTotale,
        long boutiquesActives,
        Map<String, Long> commandesParStatut,
        List<ShopRevenueResponse> topBoutiques) {
}
