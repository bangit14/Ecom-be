package com.vn.product.web.product.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vn.product.core.exception.AppException;
import com.vn.product.core.exception.ErrorCode;
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
@TableName(value = "product_variants", schema = "ecommerce_product")
public class ProductVariant {

    @TableId(type = IdType.INPUT)
    private UUID id;

    private UUID productId;
    private String sku;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private Integer stockQuantity;
    private Integer reservedQuantity;
    private String options;
    private Integer weightGrams;
    private Boolean isDefault;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Builder.Default
    private Product product = null;

    @TableField(exist = false)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    public boolean isDefault() {
        return Boolean.TRUE.equals(isDefault);
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean hasEnoughStock(Integer quantity) {
        int available = (stockQuantity != null ? stockQuantity : 0) - (reservedQuantity != null ? reservedQuantity : 0);
        return available >= (quantity != null ? quantity : 0);
    }

    public void reduceStock(Integer quantity) {
        if (!hasEnoughStock(quantity)) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }
        this.stockQuantity = (this.stockQuantity != null ? this.stockQuantity : 0) - quantity;
    }

    public void increaseStock(Integer quantity) {
        this.stockQuantity = (this.stockQuantity != null ? this.stockQuantity : 0) + quantity;
    }
}
