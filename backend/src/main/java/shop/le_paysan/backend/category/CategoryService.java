package shop.le_paysan.backend.category;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.category.dto.CategoryResponse;
import shop.le_paysan.backend.category.dto.CreateCategoryRequest;

@Service
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        return repository.findByStatus(Category.STATUS_VALIDE).stream().map(CategoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listForAdmin() {
        return repository.findAll().stream().map(CategoryResponse::from).toList();
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        Category category = createEntity(request.name());
        category.setStatus(Category.STATUS_VALIDE);
        return CategoryResponse.from(repository.save(category));
    }

    /** Suggestion par un vendeur depuis le formulaire produit : utilisable immédiatement par
     * lui (son produit référence déjà l'id), mais invisible des autres tant qu'un admin ne
     * l'a pas validée (cf. docs/07-plan-execution.md). */
    @Transactional
    public CategoryResponse suggest(CreateCategoryRequest request) {
        Category category = createEntity(request.name());
        category.setStatus(Category.STATUS_EN_ATTENTE);
        return CategoryResponse.from(repository.save(category));
    }

    @Transactional
    public CategoryResponse validate(Long id) {
        Category category = repository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
        category.setStatus(Category.STATUS_VALIDE);
        return CategoryResponse.from(repository.save(category));
    }

    private Category createEntity(String name) {
        if (repository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Cette catégorie existe déjà");
        }
        Category category = new Category();
        category.setName(name);
        category.setSlug(slugify(name));
        return category;
    }

    private static String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String withoutAccents = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        return withoutAccents.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
