package com.vn.product.web.product.domain.request;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class ProductUpdateRequest {
    private UUID categoryId;
    private String name;
    private String slug;
    private String description;
    private String brand;
    private String status;
    private String attributes;

    private List<ProductVariantUpdateRequest> variants;
    private List<ProductImageCreateRequest> images;
}
