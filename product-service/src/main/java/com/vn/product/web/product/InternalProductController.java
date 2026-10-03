package com.vn.product.web.product;

import com.vn.product.core.exception.AppException;
import com.vn.product.core.exception.ErrorCode;
import com.vn.product.web.product.domain.entity.ProductVariant;
import com.vn.product.web.product.domain.response.ProductVariantResponse;
import com.vn.product.web.product.mapper.ProductVariantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/products")
@RequiredArgsConstructor
@Slf4j(topic = "InternalProductController")
public class InternalProductController {

    private final ProductVariantMapper productVariantMapper;

    @GetMapping("/variants/{variantId}")
    public ResponseEntity<ProductVariantResponse> getVariantById(@PathVariable UUID variantId) {
        ProductVariant variant = productVariantMapper.selectById(variantId);
        if (variant == null) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND);
        }

        return ResponseEntity.ok(ProductVariantResponse.builder()
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
                .build());
    }

    @PostMapping("/variants/{variantId}/stock/reserve")
    @Transactional
    public ResponseEntity<Void> reserveStock(@PathVariable UUID variantId, @RequestParam Integer quantity) {
        ProductVariant variant = productVariantMapper.selectById(variantId);
        if (variant == null) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND);
        }

        if (!variant.hasEnoughStock(quantity)) {
            log.warn("Insufficient stock for variant {}. Available: {}, Requested: {}", variantId, variant.getStockQuantity(), quantity);
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }

        variant.reduceStock(quantity);
        productVariantMapper.updateById(variant);
        log.info("Reserved {} stock for variant {}. Remaining: {}", quantity, variantId, variant.getStockQuantity());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/variants/{variantId}/stock/release")
    @Transactional
    public ResponseEntity<Void> releaseStock(@PathVariable UUID variantId, @RequestParam Integer quantity) {
        ProductVariant variant = productVariantMapper.selectById(variantId);
        if (variant == null) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND);
        }

        variant.increaseStock(quantity);
        productVariantMapper.updateById(variant);
        log.info("Released {} stock back to variant {}. Current stock: {}", quantity, variantId, variant.getStockQuantity());
        return ResponseEntity.ok().build();
    }
}
