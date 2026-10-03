package com.vn.product.web.category.domain.request;

import lombok.Data;
import java.util.UUID;

@Data
public class CategoryUpdateRequest {
    private String name;
    private String slug;
    private String description;
    private UUID parentId;
    private String imageUrl;
    private Integer sortOrder;
    private Boolean isActive;
}
