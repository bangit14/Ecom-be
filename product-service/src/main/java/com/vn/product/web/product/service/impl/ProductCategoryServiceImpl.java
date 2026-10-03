package com.vn.product.web.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vn.product.web.category.domain.entity.Category;
import com.vn.product.web.category.domain.response.CategoryResponse;
import com.vn.product.web.category.mapper.CategoryMapper;
import com.vn.product.web.product.domain.entity.ProductCategory;
import com.vn.product.web.product.mapper.ProductCategoryMapper;
import com.vn.product.web.product.service.IProductCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "Product-Category-Service")
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements IProductCategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignCategoriesToProduct(UUID productId, List<UUID> categoryIds, UUID primaryCategoryId) {
        if (categoryIds == null || categoryIds.isEmpty()) return;

        baseMapper.delete(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getProductId, productId));

        for (UUID categoryId : categoryIds) {
            ProductCategory productCategory = ProductCategory.builder()
                    .id(UUID.randomUUID())
                    .productId(productId)
                    .categoryId(categoryId)
                    .isPrimary(categoryId.equals(primaryCategoryId))
                    .createdAt(LocalDateTime.now())
                    .build();
            baseMapper.insert(productCategory);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAllCategoriesFromProduct(UUID productId) {
        baseMapper.delete(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getProductId, productId));
    }

    @Override
    public List<CategoryResponse> getCategoriesByProductId(UUID productId) {
        List<ProductCategory> productCategories = baseMapper.selectList(
                new LambdaQueryWrapper<ProductCategory>().eq(ProductCategory::getProductId, productId));

        if (productCategories.isEmpty()) return Collections.emptyList();

        List<UUID> categoryIds = productCategories.stream().map(ProductCategory::getCategoryId).toList();
        List<Category> categories = categoryMapper.selectBatchIds(categoryIds);

        return categories.stream().map(cat -> CategoryResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .slug(cat.getSlug())
                .description(cat.getDescription())
                .imageUrl(cat.getImageUrl())
                .build()).toList();
    }
}
