package com.vn.product.web.wishlist;

import com.vn.product.core.exception.AppException;
import com.vn.product.core.exception.ErrorCode;
import com.vn.product.core.model.PageResponse;
import com.vn.product.core.security.UserContext;
import com.vn.product.core.util.PageableUtils;
import com.vn.product.web.product.domain.response.ProductResponse;
import com.vn.product.web.wishlist.service.IWishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/wishlists")
@RequiredArgsConstructor
public class WishlistController {

    private final IWishlistService wishlistService;

    @GetMapping("/my")
    public ResponseEntity<PageResponse<ProductResponse>> getMyWishlist(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UUID userId = UserContext.getUserId();
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        Pageable pageable = PageableUtils.createPageable(page, size, "createdAt", "desc");
        return ResponseEntity.ok(wishlistService.getWishlistItems(userId, pageable));
    }

    @PostMapping("/toggle")
    public ResponseEntity<String> toggleWishlistItem(
            @RequestParam UUID productId,
            @RequestParam(required = false) UUID variantId) {
        UUID userId = UserContext.getUserId();
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String message = wishlistService.toggleWishlist(userId, productId, variantId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(message);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkWishlist(@RequestParam UUID productId) {
        UUID userId = UserContext.getUserId();
        if (userId == null) {
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(wishlistService.isInWishlist(userId, productId));
    }
}
