package com.vn.product.web.category;

import com.vn.product.core.model.PageResponse;
import com.vn.product.core.util.PageableUtils;
import com.vn.product.web.category.domain.request.CategoryCreateRequest;
import com.vn.product.web.category.domain.request.CategoryFilter;
import com.vn.product.web.category.domain.request.CategoryUpdateRequest;
import com.vn.product.web.category.domain.response.CategoryResponse;
import com.vn.product.web.category.domain.response.CategorySelectResponse;
import com.vn.product.web.category.domain.response.CategoryTreeResponse;
import com.vn.product.web.category.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping("/list")
    public ResponseEntity<PageResponse<CategoryResponse>> getAllCategories(
            @ModelAttribute CategoryFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {

        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, sortDir);
        return ResponseEntity.ok(categoryService.getAllCategories(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<CategoryResponse>> getSubcategories(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getChildCategories(id));
    }

    @GetMapping("/tree")
    public ResponseEntity<List<CategoryTreeResponse>> getCategoryTree() {
        return ResponseEntity.ok(categoryService.getCategoryTree());
    }

    @GetMapping("/available-parents")
    public ResponseEntity<List<CategorySelectResponse>> getAvailableParents(
            @RequestParam(required = false) UUID excludeCategoryId) {
        return ResponseEntity.ok(categoryService.getAvailableParents(excludeCategoryId));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCategory(@PathVariable UUID id, @RequestBody CategoryUpdateRequest request) {
        categoryService.updateCategory(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
