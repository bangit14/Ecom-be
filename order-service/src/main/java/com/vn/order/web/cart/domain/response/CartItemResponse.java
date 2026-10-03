package com.vn.order.web.cart.domain.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.vn.order.core.enums.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long productId;
    private Long variantId;
    private String sku;
    private String productName;
    private JsonNode variantAttributes;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private Integer quantity;
    private Integer quantityStock;
    private StockStatus stockStatus;
    private BigDecimal subtotal;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
}
