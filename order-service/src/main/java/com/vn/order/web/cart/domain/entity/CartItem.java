package com.vn.order.web.cart.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItem {

    private Long productId;
    private Long variantId;
    private String sku;
    private String productName;
    private JsonNode variantAttributes;
    private Integer quantityStock;
    private StockStatus stockStatus;
    private String imageUrl;

    private Integer quantity;
    private BigDecimal price;
    private BigDecimal compareAtPrice;

    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;

    public BigDecimal getSubtotal() {
        return price != null && quantity != null
                ? price.multiply(BigDecimal.valueOf(quantity))
                : BigDecimal.ZERO;
    }
}
