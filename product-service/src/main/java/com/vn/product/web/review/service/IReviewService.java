package com.vn.product.web.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vn.product.core.enums.ReviewStatus;
import com.vn.product.core.model.PageResponse;
import com.vn.product.web.review.domain.entity.Review;
import com.vn.product.web.review.domain.request.ReviewCreateRequest;
import com.vn.product.web.review.domain.request.ReviewUpdateRequest;
import com.vn.product.web.review.domain.response.ReviewResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IReviewService extends IService<Review> {

    ReviewResponse createReview(UUID userId, ReviewCreateRequest request);

    ReviewResponse updateReview(UUID userId, UUID id, ReviewUpdateRequest request);

    void deleteReview(UUID userId, UUID id);

    PageResponse<ReviewResponse> getReviewsByProduct(UUID productId, Pageable pageable);

    PageResponse<ReviewResponse> getAllReviews(ReviewStatus status, Pageable pageable);

    ReviewResponse approveReview(UUID id);

    ReviewResponse rejectReview(UUID id);
}
