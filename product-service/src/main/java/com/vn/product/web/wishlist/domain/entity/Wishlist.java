package com.vn.product.web.wishlist.domain.entity;

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
@TableName(value = "wishlists", schema = "ecommerce_product")
public class Wishlist {

    @TableId(type = IdType.INPUT)
    private UUID id;

    private UUID userId;
    private UUID productId;
    private UUID variantId;
    private LocalDateTime createdAt;
}
