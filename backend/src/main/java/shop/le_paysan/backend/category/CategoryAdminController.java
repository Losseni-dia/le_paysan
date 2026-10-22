package shop.le_paysan.backend.category;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import shop.le_paysan.backend.category.dto.CategoryResponse;
import shop.le_paysan.backend.category.dto.CreateCategoryRequest;

@RestController
@RequestMapping("/api/v1/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class CategoryAdminController {

    private final CategoryService service;

    public CategoryAdminController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoryResponse> list() {
        return service.listForAdmin();
    }

    @PostMapping("/{id}/validate")
    public CategoryResponse validate(@PathVariable Long id) {
        return service.validate(id);
    }

    @PatchMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CreateCategoryRequest request) {
        return service.update(id, request.name());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
