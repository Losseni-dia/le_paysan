package shop.le_paysan.backend.product;

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

    /**
     * Cf. demande utilisateur : enrichit la recherche avec des synonymes (ex: "poisson" doit aussi
     * remonter "tilapia"/"carpe") et verifie aussi le nom de la categorie, pas seulement le produit.
     */
    public static Specification<Product> nameContains(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            var terms = SearchSynonyms.expand(search);
            var predicates = terms.stream()
                    .map(term -> cb.like(cb.lower(root.get("name")), "%" + term.toLowerCase() + "%"))
                    .toArray(jakarta.persistence.criteria.Predicate[]::new);
            var categoryMatch = cb.like(cb.lower(root.get("category").get("name")), "%" + search.toLowerCase() + "%");
            return cb.or(cb.or(predicates), categoryMatch);
        };
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
