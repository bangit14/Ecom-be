package com.vn.product.web.review.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vn.product.core.enums.ReviewStatus;
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
@TableName(value = "reviews", schema = "ecommerce_product")
public class Review {

    @TableId(type = IdType.INPUT)
    private UUID id;

    private UUID productId;
    private UUID userId;
    private UUID orderItemId;
    private Integer rating;
    private String title;
    private String content;
    private Boolean isVerifiedPurchase;
    private String status;
    private Integer helpfulCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isApproved() {
        return ReviewStatus.APPROVED.name().equalsIgnoreCase(status);
    }
}
