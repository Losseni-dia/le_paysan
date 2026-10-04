package shop.le_paysan.backend.vendor.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record VendorStatsResponse(
        BigDecimal chiffreAffairesNet, Map<String, Long> commandesParStatut, List<ProductSalesResponse> topProduits) {
}
