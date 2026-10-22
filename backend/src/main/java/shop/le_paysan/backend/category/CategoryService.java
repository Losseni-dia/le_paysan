package shop.le_paysan.backend.category;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.category.dto.CategoryResponse;
import shop.le_paysan.backend.category.dto.CreateCategoryRequest;
import shop.le_paysan.backend.product.ProductRepository;

@Service
public class CategoryService {

    private final CategoryRepository repository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository repository, ProductRepository productRepository) {
        this.repository = repository;
        this.productRepository = productRepository;
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

    @Transactional
    public CategoryResponse update(Long id, String name) {
        Category category = repository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette catégorie existe déjà");
        }
        category.setName(name);
        category.setSlug(slugify(name));
        return CategoryResponse.from(repository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        Category category = repository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (productRepository.existsByCategoryId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Cette catégorie est utilisée par des produits, impossible de la supprimer");
        }
        repository.delete(category);
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
