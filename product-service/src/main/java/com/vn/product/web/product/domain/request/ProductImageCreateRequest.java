package com.vn.product.web.product.domain.request;

import lombok.Data;
import java.util.UUID;

@Data
public class ProductImageCreateRequest {
    private UUID variantId;
    private String storageProvider;
    private String bucket;
    private String objectKey;
    private String url;
    private String mimeType;
    private Integer fileSizeBytes;
    private Integer width;
    private Integer height;
    private String altText;
    private Integer sortOrder;
    private Boolean isPrimary;
}
