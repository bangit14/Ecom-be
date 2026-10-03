package com.vn.product.web.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vn.product.core.exception.AppException;
import com.vn.product.core.exception.ErrorCode;
import com.vn.product.web.product.domain.entity.Product;
import com.vn.product.web.product.domain.entity.ProductVariant;
import com.vn.product.web.product.domain.request.ProductVariantCreateRequest;
import com.vn.product.web.product.domain.request.ProductVariantUpdateRequest;
import com.vn.product.web.product.domain.response.ProductVariantResponse;
import com.vn.product.web.product.mapper.ProductMapper;
import com.vn.product.web.product.mapper.ProductVariantMapper;
import com.vn.product.web.product.service.IProductVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "Product-Variant-Service")
public class ProductVariantServiceImpl extends ServiceImpl<ProductVariantMapper, ProductVariant> implements IProductVariantService {

    private final ProductMapper productMapper;

    @Override
    public List<ProductVariantResponse> getVariantsByProductId(UUID productId) {
        List<ProductVariant> variants = baseMapper.selectList(
                new LambdaQueryWrapper<ProductVariant>()
                        .eq(ProductVariant::getProductId, productId)
                        .orderByDesc(ProductVariant::getIsDefault));

        return variants.stream().map(
                v -> ProductVariantResponse.builder()
                        .id(v.getId())
                        .productId(v.getProductId())
                        .sku(v.getSku())
                        .price(v.getPrice())
                        .compareAtPrice(v.getCompareAtPrice())
                        .stockQuantity(v.getStockQuantity())
                        .reservedQuantity(v.getReservedQuantity())
                        .options(v.getOptions())
                        .weightGrams(v.getWeightGrams())
                        .isDefault(v.isDefault())
                        .isActive(v.getIsActive())
                        .createdAt(v.getCreatedAt())
                        .updatedAt(v.getUpdatedAt())
                        .build()
        ).toList();
    }

    @Override
    @Transactional(rollbackFor = AppException.class)
    public ProductVariantResponse createVariant(UUID productId, ProductVariantCreateRequest request) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            baseMapper.resetDefaultVariant(productId);
        }

        ProductVariant variant = ProductVariant.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .sku(request.getSku() != null ? request.getSku() : "SKU-" + System.currentTimeMillis())
                .price(request.getPrice())
                .compareAtPrice(request.getCompareAtPrice())
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .reservedQuantity(0)
                .options(request.getOptions())
                .weightGrams(request.getWeightGrams())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        baseMapper.insert(variant);

        return ProductVariantResponse.builder()
                .id(variant.getId())
                .productId(variant.getProductId())
                .sku(variant.getSku())
                .price(variant.getPrice())
                .compareAtPrice(variant.getCompareAtPrice())
                .stockQuantity(variant.getStockQuantity())
                .reservedQuantity(variant.getReservedQuantity())
                .options(variant.getOptions())
                .weightGrams(variant.getWeightGrams())
                .isDefault(variant.isDefault())
                .isActive(variant.getIsActive())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(rollbackFor = AppException.class)
    public void updateVariant(UUID variantId, ProductVariantUpdateRequest request) {
        ProductVariant variant = baseMapper.selectById(variantId);
        if (variant == null) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND);
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            baseMapper.resetDefaultVariant(variant.getProductId());
        }

        if (request.getSku() != null) variant.setSku(request.getSku());
        if (request.getPrice() != null) variant.setPrice(request.getPrice());
        if (request.getCompareAtPrice() != null) variant.setCompareAtPrice(request.getCompareAtPrice());
        if (request.getStockQuantity() != null) variant.setStockQuantity(request.getStockQuantity());
        if (request.getWeightGrams() != null) variant.setWeightGrams(request.getWeightGrams());
        if (request.getOptions() != null) variant.setOptions(request.getOptions());
        if (request.getIsDefault() != null) variant.setDefault(request.getIsDefault());
        if (request.getIsActive() != null) variant.setIsActive(request.getIsActive());
        variant.setUpdatedAt(LocalDateTime.now());

        baseMapper.updateById(variant);
    }

    @Override
    @Transactional(rollbackFor = AppException.class)
    public void deleteVariant(UUID variantId) {
        ProductVariant variant = baseMapper.selectById(variantId);
        if (variant == null) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND);
        }

        if (variant.getStockQuantity() != null && variant.getStockQuantity() > 0) {
            throw new AppException(ErrorCode.CANNOT_DELETE_VARIANT_WITH_STOCK);
        }

        baseMapper.deleteById(variantId);
    }

    @Override
    @Transactional(rollbackFor = AppException.class)
    public void deleteVariantsByProductId(UUID productId) {
        baseMapper.delete(new LambdaQueryWrapper<ProductVariant>().eq(ProductVariant::getProductId, productId));
    }
}
