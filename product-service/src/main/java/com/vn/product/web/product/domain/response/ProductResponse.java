package com.vn.product.web.product.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private UUID id;
    private UUID storeId;
    private UUID categoryId;
    private String categoryName;
    private String name;
    private String slug;
    private String description;
    private String brand;
    private String status;
    private String mainImageUrl;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer totalStock;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
