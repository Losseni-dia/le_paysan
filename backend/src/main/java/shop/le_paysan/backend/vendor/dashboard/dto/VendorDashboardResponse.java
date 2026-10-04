package shop.le_paysan.backend.vendor.dashboard.dto;

import java.math.BigDecimal;

public record VendorDashboardResponse(
        long produitsActifs,
        long produitsEnAttente,
        long commandesATraiter,
        BigDecimal chiffreAffairesNet) {
}
