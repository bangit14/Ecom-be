package com.vn.product.web.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vn.product.web.category.domain.response.CategoryResponse;
import com.vn.product.web.product.domain.entity.ProductCategory;

import java.util.List;
import java.util.UUID;

public interface IProductCategoryService extends IService<ProductCategory> {

    void assignCategoriesToProduct(UUID productId, List<UUID> categoryIds, UUID primaryCategoryId);

    void removeAllCategoriesFromProduct(UUID productId);

    List<CategoryResponse> getCategoriesByProductId(UUID productId);
}
