package com.vn.order.web.cart.service;

import com.vn.order.web.cart.domain.entity.Cart;
import com.vn.order.web.cart.domain.request.AddCartItemRequest;
import com.vn.order.web.cart.domain.request.UpdateCartItemRequest;
import com.vn.order.web.cart.domain.response.CartResponse;

import java.util.Set;

public interface ICartService {

    CartResponse getCart(Long userId);

    Cart getCartDomain(Long userId);

    void addItem(Long userId, AddCartItemRequest request);

    void updateQuantity(Long userId, UpdateCartItemRequest request);

    void removeItem(Long userId, Long variantId);

    void removeItems(Long userId, Set<Long> variantIds);

    void clearCart(Long userId);

    int countItems(Long userId);

    boolean existsItem(Long userId, Long variantId);
}
