package com.vn.product.web.product.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vn.product.core.enums.ProductStatus;
import com.vn.product.web.category.domain.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "products", schema = "ecommerce_product")
public class Product {

    @TableId(type = IdType.INPUT)
    private UUID id;

    private UUID storeId;
    private UUID categoryId;
    private String name;
    private String slug;
    private String description;
    private String brand;
    private String status;
    private String attributes;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Builder.Default
    private Category category = null;

    @TableField(exist = false)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @TableField(exist = false)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    public boolean isActive() {
        return ProductStatus.ACTIVE.name().equalsIgnoreCase(status);
    }

    public ProductVariant getDefaultVariant() {
        if (variants == null || variants.isEmpty()) {
            return null;
        }
        return variants.stream()
                .filter(ProductVariant::isDefault)
                .findFirst()
                .orElse(variants.get(0));
    }
}
