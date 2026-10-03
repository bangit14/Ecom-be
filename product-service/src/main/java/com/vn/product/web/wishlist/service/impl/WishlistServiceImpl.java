package com.vn.product.web.wishlist.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vn.product.core.exception.AppException;
import com.vn.product.core.exception.ErrorCode;
import com.vn.product.core.model.PageResponse;
import com.vn.product.web.category.domain.entity.Category;
import com.vn.product.web.category.mapper.CategoryMapper;
import com.vn.product.web.product.domain.entity.Product;
import com.vn.product.web.product.domain.entity.ProductImage;
import com.vn.product.web.product.domain.entity.ProductVariant;
import com.vn.product.web.product.domain.response.ProductResponse;
import com.vn.product.web.product.mapper.ProductImageMapper;
import com.vn.product.web.product.mapper.ProductMapper;
import com.vn.product.web.product.mapper.ProductVariantMapper;
import com.vn.product.web.wishlist.domain.entity.Wishlist;
import com.vn.product.web.wishlist.mapper.WishlistMapper;
import com.vn.product.web.wishlist.service.IWishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "Wishlist-Service")
public class WishlistServiceImpl extends ServiceImpl<WishlistMapper, Wishlist> implements IWishlistService {

    private final ProductMapper productMapper;
    private final ProductVariantMapper productVariantMapper;
    private final ProductImageMapper productImageMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public boolean isInWishlist(UUID userId, UUID productId) {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<Wishlist>()
                .eq(Wishlist::getUserId, userId)
                .eq(Wishlist::getProductId, productId));
        return count != null && count > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String toggleWishlist(UUID userId, UUID productId, UUID variantId) {
        if (isInWishlist(userId, productId)) {
            baseMapper.delete(new LambdaQueryWrapper<Wishlist>()
                    .eq(Wishlist::getUserId, userId)
                    .eq(Wishlist::getProductId, productId));
            log.debug("Removed product {} from wishlist of user {}", productId, userId);
            return "Đã xóa sản phẩm khỏi mục yêu thích";
        } else {
            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            Wishlist wishlist = Wishlist.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .productId(productId)
                    .variantId(variantId)
                    .createdAt(LocalDateTime.now())
                    .build();

            baseMapper.insert(wishlist);
            log.debug("Added product {} to wishlist of user {}", productId, userId);
            return "Đã thêm sản phẩm vào mục yêu thích";
        }
    }

    @Override
    public PageResponse<ProductResponse> getWishlistItems(UUID userId, Pageable pageable) {
        Page<Wishlist> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<Wishlist> result = baseMapper.selectPage(page, new LambdaQueryWrapper<Wishlist>()
                .eq(Wishlist::getUserId, userId)
                .orderByDesc(Wishlist::getCreatedAt));

        List<Wishlist> wishlists = result.getRecords();
        if (wishlists == null || wishlists.isEmpty()) {
            return PageResponse.<ProductResponse>builder()
                    .content(Collections.emptyList())
                    .pageNumber((int) result.getCurrent())
                    .pageSize((int) result.getSize())
                    .totalElements(result.getTotal())
                    .totalPages((int) result.getPages())
                    .last(true)
                    .first(true)
                    .empty(true)
                    .build();
        }

        List<UUID> productIds = wishlists.stream().map(Wishlist::getProductId).toList();
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<UUID, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<ProductVariant> variants = productVariantMapper.selectList(
                new LambdaQueryWrapper<ProductVariant>().in(ProductVariant::getProductId, productIds));
        Map<UUID, List<ProductVariant>> variantsByProductId = variants.stream()
                .collect(Collectors.groupingBy(ProductVariant::getProductId));

        List<ProductImage> images = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>().in(ProductImage::getProductId, productIds).eq(ProductImage::getIsPrimary, true));
        Map<UUID, ProductImage> mainImageByProductId = images.stream()
                .collect(Collectors.toMap(ProductImage::getProductId, img -> img, (existing, replacement) -> existing));

        List<UUID> categoryIds = products.stream().map(Product::getCategoryId).filter(Objects::nonNull).toList();
        Map<UUID, Category> categoryById = categoryIds.isEmpty() ? Collections.emptyMap() :
                categoryMapper.selectBatchIds(categoryIds).stream().collect(Collectors.toMap(Category::getId, c -> c));

        List<ProductResponse> content = wishlists.stream()
                .map(w -> productMap.get(w.getProductId()))
                .filter(Objects::nonNull)
                .map(product -> {
                    Category cat = product.getCategoryId() != null ? categoryById.get(product.getCategoryId()) : null;
                    ProductImage mainImg = mainImageByProductId.get(product.getId());
                    List<ProductVariant> prodVariants = variantsByProductId.getOrDefault(product.getId(), Collections.emptyList());

                    BigDecimal minPrice = null;
                    BigDecimal maxPrice = null;
                    for (ProductVariant v : prodVariants) {
                        if (v.getPrice() != null) {
                            if (minPrice == null || v.getPrice().compareTo(minPrice) < 0) minPrice = v.getPrice();
                            if (maxPrice == null || v.getPrice().compareTo(maxPrice) > 0) maxPrice = v.getPrice();
                        }
                    }

                    return ProductResponse.builder()
                            .id(product.getId())
                            .storeId(product.getStoreId())
                            .categoryId(product.getCategoryId())
                            .categoryName(cat != null ? cat.getName() : null)
                            .name(product.getName())
                            .slug(product.getSlug())
                            .description(product.getDescription())
                            .brand(product.getBrand())
                            .status(product.getStatus())
                            .mainImageUrl(mainImg != null ? mainImg.getUrl() : null)
                            .minPrice(minPrice)
                            .maxPrice(maxPrice)
                            .ratingAvg(product.getRatingAvg())
                            .ratingCount(product.getRatingCount())
                            .createdAt(product.getCreatedAt())
                            .updatedAt(product.getUpdatedAt())
                            .build();
                })
                .toList();

        return PageResponse.<ProductResponse>builder()
                .content(content)
                .pageNumber((int) result.getCurrent())
                .pageSize((int) result.getSize())
                .totalElements(result.getTotal())
                .totalPages((int) result.getPages())
                .last(result.getCurrent() >= result.getPages())
                .first(result.getCurrent() <= 1)
                .empty(content.isEmpty())
                .build();
    }
}
