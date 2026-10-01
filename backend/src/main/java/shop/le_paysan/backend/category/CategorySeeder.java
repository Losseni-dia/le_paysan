package shop.le_paysan.backend.category;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import shop.le_paysan.backend.category.dto.CreateCategoryRequest;

/** Categories de depart (cf. docs/07-plan-execution.md Phase 3). */
@Component
public class CategorySeeder implements CommandLineRunner {

    private static final String[] DEFAULT_CATEGORIES = {
        "Maraîchage", "Fruits", "Légumes", "Céréales", "Élevage", "Produits laitiers", "Produits transformés", "Épices et condiments"
    };

    private final CategoryRepository repository;
    private final CategoryService categoryService;

    public CategorySeeder(CategoryRepository repository, CategoryService categoryService) {
        this.repository = repository;
        this.categoryService = categoryService;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        for (String name : DEFAULT_CATEGORIES) {
            categoryService.create(new CreateCategoryRequest(name));
        }
    }
}
