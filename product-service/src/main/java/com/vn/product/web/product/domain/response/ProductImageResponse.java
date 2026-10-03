package com.vn.product.web.product.domain.response;

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
public class ProductImageResponse {
    private UUID id;
    private UUID productId;
    private UUID variantId;
    private String storageProvider;
    private String bucket;
    private String objectKey;
    private String url;
    private String altText;
    private Integer sortOrder;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
}
