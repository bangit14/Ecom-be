package com.vn.product.web.product.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName(value = "product_categories", schema = "ecommerce_product")
public class ProductCategory {

    @TableId(type = IdType.INPUT)
    private UUID id;

    private UUID productId;
    private UUID categoryId;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
}
