package shop.le_paysan.backend.vendor.dashboard.dto;

import java.math.BigDecimal;

public record ProductSalesResponse(String productName, long quantitySold, BigDecimal revenue) {
}
