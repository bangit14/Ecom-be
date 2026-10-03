package com.vn.product.web.product.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "product_images", schema = "ecommerce_product")
public class ProductImage {

    @TableId(type = IdType.INPUT)
    private UUID id;

    private UUID productId;
    private UUID variantId;
    private String storageProvider;
    private String bucket;
    private String objectKey;
    private String url;
    private String mimeType;
    private Integer fileSizeBytes;
    private Integer width;
    private Integer height;
    private String altText;
    private Integer sortOrder;
    private Boolean isPrimary;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    @Builder.Default
    private Product product = null;

    @TableField(exist = false)
    @Builder.Default
    private ProductVariant variant = null;

    public boolean isPrimary() {
        return Boolean.TRUE.equals(isPrimary);
    }

    public void setPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
}
