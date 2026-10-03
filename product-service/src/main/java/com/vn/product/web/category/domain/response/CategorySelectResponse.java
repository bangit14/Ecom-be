package com.vn.product.web.category.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySelectResponse {
    private UUID id;
    private String name;
    private UUID parentId;
}
