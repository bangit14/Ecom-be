package com.vn.order.web.cart.domain.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCartItemRequest {
    private Long productId;
    private Long variantId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private String sku;
    private String productName;
    private String imageUrl;
    private JsonNode variantAttributes;
}
