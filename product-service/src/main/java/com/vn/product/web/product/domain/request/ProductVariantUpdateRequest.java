package com.vn.product.web.product.domain.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductVariantUpdateRequest {
    private UUID id;
    private String sku;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private Integer stockQuantity;
    private String options;
    private Integer weightGrams;
    private Boolean isDefault;
    private Boolean isActive;
}
