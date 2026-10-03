package com.vn.product.web.product.domain.response;

import com.vn.product.web.category.domain.response.CategoryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {
    private UUID id;
    private UUID storeId;
    private UUID categoryId;
    private CategoryResponse category;
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
    private List<ProductVariantResponse> variants;
    private List<ProductImageResponse> images;
}
