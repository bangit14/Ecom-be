package com.vn.order.web.cart;

import com.vn.order.core.exception.AppException;
import com.vn.order.core.exception.ErrorCode;
import com.vn.order.core.security.UserContext;
import com.vn.order.web.cart.domain.request.AddCartItemRequest;
import com.vn.order.web.cart.domain.request.UpdateCartItemRequest;
import com.vn.order.web.cart.domain.response.CartResponse;
import com.vn.order.web.cart.service.ICartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Slf4j(topic = "Cart-Controller")
public class CartController {

    private final ICartService cartService;

    private Long requireUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Yêu cầu đăng nhập để truy cập giỏ hàng");
        }
        return userId;
    }

    @GetMapping("/items")
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart(requireUserId()));
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addToCart(@RequestBody AddCartItemRequest request) {
        cartService.addItem(requireUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateCartItem(@RequestBody UpdateCartItemRequest request) {
        cartService.updateQuantity(requireUserId(), request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeCartItem(@RequestParam Long variantId) {
        cartService.removeItem(requireUserId(), variantId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart(requireUserId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount() {
        return ResponseEntity.ok(cartService.countItems(requireUserId()));
    }
}
