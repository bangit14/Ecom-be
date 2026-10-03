package com.vn.order.web.order.domain.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private Integer stockQuantity;
    private BigDecimal weight;
    private boolean isDefault;
    private JsonNode attributes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
