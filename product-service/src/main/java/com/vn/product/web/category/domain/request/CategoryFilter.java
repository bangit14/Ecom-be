package com.vn.product.web.category.domain.request;

import lombok.Data;
import java.util.UUID;

@Data
public class CategoryFilter {
    private String name;
    private String slug;
    private UUID parentId;
    private Boolean isActive;
}
