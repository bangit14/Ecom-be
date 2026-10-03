package com.vn.shipment.web.shipment.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentItemResponse {

    private Long id;
    private Long shipmentId;
    private Long orderItemId;
    private String productName;
    private String sku;
    private Integer quantity;
    private Integer unitWeightGram;
    private BigDecimal unitPrice;
    private String category;
}
