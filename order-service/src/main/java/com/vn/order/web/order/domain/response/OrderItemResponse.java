package com.vn.order.web.order.domain.response;

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
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private Long variantId;
    private String sku;
    private String productName;
    private JsonNode variantAttributes;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
}
