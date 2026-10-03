package com.vn.product.web.product.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageUploadRequest {
    private MultipartFile file;
    private Long variantId;
    @Builder.Default
    private boolean isMain = false;
    @Builder.Default
    private Integer sortOrder = 0;
}
