package com.vn.product.web.category.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vn.product.core.model.PageResponse;
import com.vn.product.web.category.domain.entity.Category;
import com.vn.product.web.category.domain.request.CategoryCreateRequest;
import com.vn.product.web.category.domain.request.CategoryFilter;
import com.vn.product.web.category.domain.request.CategoryUpdateRequest;
import com.vn.product.web.category.domain.response.CategoryResponse;
import com.vn.product.web.category.domain.response.CategorySelectResponse;
import com.vn.product.web.category.domain.response.CategoryTreeResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ICategoryService extends IService<Category> {

    PageResponse<CategoryResponse> getAllCategories(CategoryFilter filter, Pageable pageable);

    CategoryResponse createCategory(CategoryCreateRequest request);

    void updateCategory(UUID id, CategoryUpdateRequest request);

    void deleteCategory(UUID id);

    CategoryResponse getCategoryById(UUID id);

    List<CategorySelectResponse> getAvailableParents(UUID excludeCategoryId);

    List<CategoryResponse> getRootCategories();

    List<CategoryResponse> getChildCategories(UUID parentId);

    List<CategoryTreeResponse> getCategoryTree();

    List<CategoryTreeResponse> getCategoryTreeByParentId(UUID parentId);

    List<UUID> getAllDescendantCategoryIds(UUID categoryId);
}
