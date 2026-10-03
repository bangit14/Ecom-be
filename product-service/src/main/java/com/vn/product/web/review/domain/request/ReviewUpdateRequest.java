package com.vn.product.web.review.domain.request;

import lombok.Data;

@Data
public class ReviewUpdateRequest {
    private Integer rating;
    private String title;
    private String content;
}
