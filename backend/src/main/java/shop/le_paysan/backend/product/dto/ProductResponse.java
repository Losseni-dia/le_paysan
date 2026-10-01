package shop.le_paysan.backend.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import shop.le_paysan.backend.product.Product;
import shop.le_paysan.backend.product.ProductDeliveryOption;
import shop.le_paysan.backend.product.ProductImage;

public record ProductResponse(
        Long id,
        Long shopId,
        String shopName,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        String unit,
        BigDecimal unitPrice,
        Integer stockQuantity,
        String status,
        List<String> imageUrls,
        List<DeliveryOption> deliveryOptions,
        LocalDateTime createdAt) {

    public record DeliveryOption(String mode, String pickupLocation, BigDecimal deliveryPrice) {
        static DeliveryOption from(ProductDeliveryOption o) {
            return new DeliveryOption(o.getMode(), o.getPickupLocation(), o.getDeliveryPrice());
        }
    }

    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getShop().getId(),
                p.getShop().getName(),
                p.getCategory().getId(),
                p.getCategory().getName(),
                p.getName(),
                p.getDescription(),
                p.getUnit(),
                p.getUnitPrice(),
                p.getStockQuantity(),
                p.getStatus(),
                p.getImages().stream().map(ProductImage::getUrl).toList(),
                p.getDeliveryOptions().stream().map(DeliveryOption::from).toList(),
                p.getCreatedAt());
    }
}
