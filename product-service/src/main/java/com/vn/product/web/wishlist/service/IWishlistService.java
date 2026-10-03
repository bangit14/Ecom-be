package com.vn.product.web.wishlist.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vn.product.core.model.PageResponse;
import com.vn.product.web.product.domain.response.ProductResponse;
import com.vn.product.web.wishlist.domain.entity.Wishlist;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IWishlistService extends IService<Wishlist> {

    boolean isInWishlist(UUID userId, UUID productId);

    String toggleWishlist(UUID userId, UUID productId, UUID variantId);

    PageResponse<ProductResponse> getWishlistItems(UUID userId, Pageable pageable);
}
