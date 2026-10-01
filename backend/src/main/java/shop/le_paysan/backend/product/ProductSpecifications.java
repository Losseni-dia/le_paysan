package shop.le_paysan.backend.product;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import shop.le_paysan.backend.shop.Shop;

/** Filtres du catalogue public (cf. docs/05 §5). */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> isVisible() {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("status"), Product.STATUS_VALIDE),
                cb.equal(root.get("shop").get("status"), Shop.STATUS_VALIDEE));
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> hasShop(Long shopId) {
        return (root, query, cb) -> shopId == null ? null : cb.equal(root.get("shop").get("id"), shopId);
    }

    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min != null && max != null) {
                return cb.between(root.get("unitPrice"), min, max);
            }
            return min != null ? cb.greaterThanOrEqualTo(root.get("unitPrice"), min) : cb.lessThanOrEqualTo(root.get("unitPrice"), max);
        };
    }

    public static Specification<Product> nameContains(String search) {
        return (root, query, cb) ->
                search == null || search.isBlank() ? null : cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
    }

    public static Specification<Product> hasDeliveryMode(String mode) {
        return (root, query, cb) -> {
            if (mode == null || mode.isBlank()) {
                return null;
            }
            query.distinct(true);
            var join = root.join("deliveryOptions");
            return cb.equal(join.get("mode"), mode);
        };
    }
}
