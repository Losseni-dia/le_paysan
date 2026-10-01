package shop.le_paysan.backend.category.dto;

import shop.le_paysan.backend.category.Category;

public record CategoryResponse(Long id, String name, String slug, String status) {

    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getStatus());
    }
}
