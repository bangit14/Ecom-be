package com.vn.product.web.review.domain.request;

import lombok.Data;
import java.util.UUID;

@Data
public class ReviewCreateRequest {
    private UUID productId;
    private UUID orderItemId;
    private Integer rating;
    private String title;
    private String content;
}
