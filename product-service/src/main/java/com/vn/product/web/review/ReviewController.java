package com.vn.product.web.review;

import com.vn.product.core.enums.ReviewStatus;
import com.vn.product.core.exception.AppException;
import com.vn.product.core.exception.ErrorCode;
import com.vn.product.core.model.PageResponse;
import com.vn.product.core.security.UserContext;
import com.vn.product.core.util.PageableUtils;
import com.vn.product.web.review.domain.request.ReviewCreateRequest;
import com.vn.product.web.review.domain.request.ReviewUpdateRequest;
import com.vn.product.web.review.domain.response.ReviewResponse;
import com.vn.product.web.review.service.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;

    @PostMapping("/api/reviews")
    public ResponseEntity<ReviewResponse> createReview(@RequestBody ReviewCreateRequest request) {
        UUID userId = UserContext.getUserId();
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(userId, request));
    }

    @GetMapping("/api/reviews/product/{productId}")
    public ResponseEntity<PageResponse<ReviewResponse>> getByProduct(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageableUtils.createPageable(page, size, "createdAt", "desc");
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId, pageable));
    }

    @PutMapping("/api/reviews/{id}")
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable UUID id, @RequestBody ReviewUpdateRequest request) {
        UUID userId = UserContext.getUserId();
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return ResponseEntity.ok(reviewService.updateReview(userId, id, request));
    }

    @DeleteMapping("/api/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID id) {
        UUID userId = UserContext.getUserId();
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        reviewService.deleteReview(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/reviews")
    public ResponseEntity<PageResponse<ReviewResponse>> getAllReviews(
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageableUtils.createPageable(page, size, "createdAt", "desc");
        return ResponseEntity.ok(reviewService.getAllReviews(status, pageable));
    }

    @PutMapping("/api/admin/reviews/{id}/approve")
    public ResponseEntity<ReviewResponse> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(reviewService.approveReview(id));
    }

    @PutMapping("/api/admin/reviews/{id}/reject")
    public ResponseEntity<ReviewResponse> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(reviewService.rejectReview(id));
    }
}
