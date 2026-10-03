package com.vn.product.web.product.domain.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductFilter {
    private String keyword;
    private UUID categoryId;
    private UUID storeId;
    private String brand;
    private String status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean inStock;
}
