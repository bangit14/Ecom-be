package com.vn.shipment.web.shipment.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentItemRequest {

    private Long orderItemId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String productName;

    private String sku;

    @Builder.Default
    private Integer quantity = 1;

    private Integer unitWeightGram;
    private BigDecimal unitPrice;
    private String category;
}
