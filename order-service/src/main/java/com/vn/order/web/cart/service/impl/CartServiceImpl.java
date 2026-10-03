package com.vn.order.web.cart.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.order.core.client.ProductClient;
import com.vn.order.core.enums.StockStatus;
import com.vn.order.core.exception.AppException;
import com.vn.order.core.exception.ErrorCode;
import com.vn.order.web.cart.domain.entity.Cart;
import com.vn.order.web.cart.domain.entity.CartItem;
import com.vn.order.web.cart.domain.request.AddCartItemRequest;
import com.vn.order.web.cart.domain.request.UpdateCartItemRequest;
import com.vn.order.web.cart.domain.response.CartItemResponse;
import com.vn.order.web.cart.domain.response.CartResponse;
import com.vn.order.web.cart.service.ICartService;
import com.vn.order.web.order.domain.response.ProductVariantResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j(topic = "Cart-Service")
public class CartServiceImpl implements ICartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ProductClient productClient;

    private HashOperations<String, String, Object> hashOps;

    private static final String USER_CART_KEY_PREFIX = "cart:user:";
    private static final long CART_TTL_DAYS = 7;

    public CartServiceImpl(RedisTemplate<String, Object> redisTemplate,
                           ObjectMapper objectMapper,
                           ProductClient productClient) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.productClient = productClient;
    }

    @PostConstruct
    private void init() {
        this.hashOps = redisTemplate.opsForHash();
    }

    private String getCartKey(Long userId) {
        return USER_CART_KEY_PREFIX + userId;
    }

    private void setCartTTL(String cartKey) {
        redisTemplate.expire(cartKey, CART_TTL_DAYS, TimeUnit.DAYS);
    }

    private CartItem toCartItem(Object raw) {
        if (raw == null) return null;
        if (raw instanceof CartItem item) return item;
        return objectMapper.convertValue(raw, CartItem.class);
    }

    @Override
    public CartResponse getCart(Long userId) {
        List<CartItem> cartItems = getCartItemInternal(userId);

        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(item -> CartItemResponse.builder()
                        .productId(item.getProductId())
                        .variantId(item.getVariantId())
                        .sku(item.getSku())
                        .productName(item.getProductName())
                        .variantAttributes(item.getVariantAttributes())
                        .imageUrl(item.getImageUrl())
                        .price(item.getPrice())
                        .compareAtPrice(item.getCompareAtPrice())
                        .quantity(item.getQuantity())
                        .quantityStock(item.getQuantityStock())
                        .stockStatus(item.getStockStatus())
                        .subtotal(item.getSubtotal())
                        .addedAt(item.getAddedAt())
                        .updatedAt(item.getUpdatedAt())
                        .build())
                .toList();

        BigDecimal total = cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalQty = cartItems.stream()
                .mapToInt(i -> i.getQuantity() != null ? i.getQuantity() : 0)
                .sum();

        return CartResponse.builder()
                .userId(userId)
                .items(itemResponses)
                .totalAmount(total)
                .totalQuantity(totalQty)
                .itemCount(cartItems.size())
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Override
    public Cart getCartDomain(Long userId) {
        List<CartItem> items = getCartItemInternal(userId);
        return Cart.builder()
                .userId(userId)
                .items(items)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Override
    public void addItem(Long userId, AddCartItemRequest request) {
        if (request.getVariantId() == null || request.getQuantity() == null || request.getQuantity() <= 0) {
            log.warn("Invalid variant id or quantity for user {}", userId);
            throw new AppException(ErrorCode.INVALID_REQUEST, "Variant id và quantity không hợp lệ");
        }

        ProductVariantResponse variant = productClient.getVariant(request.getVariantId());

        if (variant.getStockQuantity() == null || variant.getStockQuantity() < request.getQuantity()) {
            log.warn("Not enough stock for variant {}. Available: {}, Requested: {}",
                    request.getVariantId(), variant.getStockQuantity(), request.getQuantity());
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }

        String cartKey = getCartKey(userId);
        String field = request.getVariantId().toString();

        CartItem existingItem = toCartItem(hashOps.get(cartKey, field));

        StockStatus stockStatus = (variant.getStockQuantity() - request.getQuantity() <= 5)
                ? StockStatus.LOW_STOCK : StockStatus.IN_STOCK;

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (newQuantity > variant.getStockQuantity()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK, "Số lượng trong giỏ vượt quá tồn kho khả dụng");
            }
            existingItem.setQuantity(newQuantity);
            existingItem.setQuantityStock(variant.getStockQuantity());
            existingItem.setStockStatus(stockStatus);
            existingItem.setPrice(variant.getPrice() != null ? variant.getPrice() : request.getPrice());
            existingItem.setUpdatedAt(LocalDateTime.now());

            hashOps.put(cartKey, field, existingItem);
            log.debug("Updated variant {} quantity to {} for user {}", field, newQuantity, userId);
        } else {
            CartItem newItem = CartItem.builder()
                    .productId(request.getProductId() != null ? request.getProductId() : variant.getProductId())
                    .variantId(request.getVariantId())
                    .sku(variant.getSku() != null ? variant.getSku() : request.getSku())
                    .productName(variant.getProductName() != null ? variant.getProductName() : request.getProductName())
                    .variantAttributes(variant.getAttributes() != null ? variant.getAttributes() : request.getVariantAttributes())
                    .imageUrl(request.getImageUrl())
                    .price(variant.getPrice() != null ? variant.getPrice() : request.getPrice())
                    .compareAtPrice(variant.getCompareAtPrice() != null ? variant.getCompareAtPrice() : request.getCompareAtPrice())
                    .quantity(request.getQuantity())
                    .quantityStock(variant.getStockQuantity())
                    .stockStatus(stockStatus)
                    .addedAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            hashOps.put(cartKey, field, newItem);
            log.debug("Added variant {} to user {}'s cart with quantity {}", field, userId, newItem.getQuantity());
        }

        setCartTTL(cartKey);
    }

    @Override
    public void updateQuantity(Long userId, UpdateCartItemRequest request) {
        if (request.getVariantId() == null) {
            log.warn("Variant id is null for update cart");
            return;
        }

        String cartKey = getCartKey(userId);
        String field = request.getVariantId().toString();

        CartItem existingItem = toCartItem(hashOps.get(cartKey, field));
        if (existingItem == null) {
            log.warn("Variant {} not found in user {}'s cart", field, userId);
            return;
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            hashOps.delete(cartKey, field);
            log.debug("Removed variant {} from cart due to 0 quantity", field);
        } else {
            existingItem.setQuantity(request.getQuantity());
            existingItem.setUpdatedAt(LocalDateTime.now());
            hashOps.put(cartKey, field, existingItem);
            log.debug("Updated variant {} quantity to {} for user {}", field, request.getQuantity(), userId);
        }

        setCartTTL(cartKey);
    }

    @Override
    public void removeItem(Long userId, Long variantId) {
        if (variantId == null) return;
        String cartKey = getCartKey(userId);
        hashOps.delete(cartKey, variantId.toString());
        setCartTTL(cartKey);
    }

    @Override
    public void removeItems(Long userId, Set<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) return;
        String cartKey = getCartKey(userId);
        String[] fields = variantIds.stream().map(String::valueOf).toArray(String[]::new);
        hashOps.delete(cartKey, (Object[]) fields);
        setCartTTL(cartKey);
    }

    @Override
    public void clearCart(Long userId) {
        String cartKey = getCartKey(userId);
        redisTemplate.delete(cartKey);
    }

    @Override
    public int countItems(Long userId) {
        String cartKey = getCartKey(userId);
        Long size = hashOps.size(cartKey);
        return size != null ? size.intValue() : 0;
    }

    @Override
    public boolean existsItem(Long userId, Long variantId) {
        String cartKey = getCartKey(userId);
        return Boolean.TRUE.equals(hashOps.hasKey(cartKey, variantId.toString()));
    }

    private List<CartItem> getCartItemInternal(Long userId) {
        String cartKey = getCartKey(userId);
        Map<String, Object> entries = hashOps.entries(cartKey);
        return entries.values().stream()
                .map(this::toCartItem)
                .toList();
    }
}
