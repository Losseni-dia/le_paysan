package shop.le_paysan.backend.admin.dashboard.dto;

import java.math.BigDecimal;

public record ShopRevenueResponse(String shopName, BigDecimal revenue) {
}
