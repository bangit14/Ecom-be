package com.vn.product.web.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vn.product.core.enums.ReviewStatus;
import com.vn.product.core.exception.AppException;
import com.vn.product.core.exception.ErrorCode;
import com.vn.product.core.model.PageResponse;
import com.vn.product.web.product.domain.entity.Product;
import com.vn.product.web.product.mapper.ProductMapper;
import com.vn.product.web.review.domain.entity.Review;
import com.vn.product.web.review.domain.request.ReviewCreateRequest;
import com.vn.product.web.review.domain.request.ReviewUpdateRequest;
import com.vn.product.web.review.domain.response.ReviewResponse;
import com.vn.product.web.review.mapper.ReviewMapper;
import com.vn.product.web.review.service.IReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "Review-Service")
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements IReviewService {

    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = AppException.class)
    public ReviewResponse createReview(UUID userId, ReviewCreateRequest request) {
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Long existing = baseMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getProductId, request.getProductId())
                .eq(Review::getUserId, userId));
        if (existing != null && existing > 0) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.builder()
                .id(UUID.randomUUID())
                .productId(request.getProductId())
                .userId(userId)
                .orderItemId(request.getOrderItemId())
                .rating(request.getRating())
                .title(request.getTitle())
                .content(request.getContent())
                .isVerifiedPurchase(request.getOrderItemId() != null)
                .status(ReviewStatus.PENDING.name())
                .helpfulCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        baseMapper.insert(review);
        log.info("Created review {} for product {} by user {}", review.getId(), request.getProductId(), userId);
        return mapToReviewResponse(review);
    }

    @Override
    @Transactional(rollbackFor = AppException.class)
    public ReviewResponse updateReview(UUID userId, UUID id, ReviewUpdateRequest request) {
        Review review = baseMapper.selectById(id);
        if (review == null) {
            throw new AppException(ErrorCode.REVIEW_NOT_FOUND);
        }
        if (!review.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        if (request.getRating() != null) review.setRating(request.getRating());
        if (request.getTitle() != null) review.setTitle(request.getTitle());
        if (request.getContent() != null) review.setContent(request.getContent());
        review.setUpdatedAt(LocalDateTime.now());

        baseMapper.updateById(review);
        recalculateProductRating(review.getProductId());
        return mapToReviewResponse(review);
    }

    @Override
    @Transactional(rollbackFor = AppException.class)
    public void deleteReview(UUID userId, UUID id) {
        Review review = baseMapper.selectById(id);
        if (review == null) {
            throw new AppException(ErrorCode.REVIEW_NOT_FOUND);
        }
        if (!review.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        UUID productId = review.getProductId();
        baseMapper.deleteById(id);
        recalculateProductRating(productId);
    }

    @Override
    public PageResponse<ReviewResponse> getReviewsByProduct(UUID productId, Pageable pageable) {
        Page<Review> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<Review>()
                .eq(Review::getProductId, productId)
                .eq(Review::getStatus, ReviewStatus.APPROVED.name())
                .orderByDesc(Review::getCreatedAt);

        IPage<Review> result = baseMapper.selectPage(page, wrapper);
        List<ReviewResponse> responses = result.getRecords().stream().map(this::mapToReviewResponse).toList();

        return PageResponse.<ReviewResponse>builder()
                .content(responses)
                .pageNumber((int) result.getCurrent())
                .pageSize((int) result.getSize())
                .totalElements(result.getTotal())
                .totalPages((int) result.getPages())
                .last(result.getCurrent() >= result.getPages())
                .first(result.getCurrent() <= 1)
                .empty(responses.isEmpty())
                .build();
    }

    @Override
    public PageResponse<ReviewResponse> getAllReviews(ReviewStatus status, Pageable pageable) {
        Page<Review> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<Review>()
                .orderByDesc(Review::getCreatedAt);

        if (status != null) {
            wrapper.eq(Review::getStatus, status.name());
        }

        IPage<Review> result = baseMapper.selectPage(page, wrapper);
        List<ReviewResponse> responses = result.getRecords().stream().map(this::mapToReviewResponse).toList();

        return PageResponse.<ReviewResponse>builder()
                .content(responses)
                .pageNumber((int) result.getCurrent())
                .pageSize((int) result.getSize())
                .totalElements(result.getTotal())
                .totalPages((int) result.getPages())
                .last(result.getCurrent() >= result.getPages())
                .first(result.getCurrent() <= 1)
                .empty(responses.isEmpty())
                .build();
    }

    @Override
    @Transactional(rollbackFor = AppException.class)
    public ReviewResponse approveReview(UUID id) {
        Review review = baseMapper.selectById(id);
        if (review == null) {
            throw new AppException(ErrorCode.REVIEW_NOT_FOUND);
        }

        review.setStatus(ReviewStatus.APPROVED.name());
        review.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(review);

        recalculateProductRating(review.getProductId());
        return mapToReviewResponse(review);
    }

    @Override
    @Transactional(rollbackFor = AppException.class)
    public ReviewResponse rejectReview(UUID id) {
        Review review = baseMapper.selectById(id);
        if (review == null) {
            throw new AppException(ErrorCode.REVIEW_NOT_FOUND);
        }

        review.setStatus(ReviewStatus.REJECTED.name());
        review.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(review);

        recalculateProductRating(review.getProductId());
        return mapToReviewResponse(review);
    }

    private void recalculateProductRating(UUID productId) {
        try {
            BigDecimal avg = baseMapper.calculateAverageRating(productId);
            Integer count = baseMapper.countApprovedReviews(productId);

            Product product = productMapper.selectById(productId);
            if (product != null) {
                product.setRatingAvg(avg != null ? avg : BigDecimal.ZERO);
                product.setRatingCount(count != null ? count : 0);
                product.setUpdatedAt(LocalDateTime.now());
                productMapper.updateById(product);
            }
        } catch (Exception e) {
            log.error("Failed to recalculate rating for productId={}: {}", productId, e.getMessage());
        }
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .userId(review.getUserId())
                .orderItemId(review.getOrderItemId())
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .isVerifiedPurchase(review.getIsVerifiedPurchase())
                .status(review.getStatus())
                .helpfulCount(review.getHelpfulCount())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
