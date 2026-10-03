package com.vn.shipment.core.rabbitmq.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedShipmentEvent {

    private Long orderId;
    private String orderCode;
    private String providerCode; // GHN, GHTK, AHAMOVE, GOSHIP
    private String serviceCode;  // GHN_STANDARD, GHTK_STANDARD, etc.

    // Sender
    private String senderName;
    private String senderPhone;
    private String senderAddress;
    private String senderWard;
    private String senderDistrict;
    private String senderProvince;

    // Receiver
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String receiverWard;
    private String receiverDistrict;
    private String receiverProvince;

    private BigDecimal codAmount;
    private BigDecimal insuranceAmount;
    private BigDecimal shippingFee;
    private Integer totalWeightGram;
    private BigDecimal totalValue;
    private String note;

    private List<ShipmentItemEventDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShipmentItemEventDto {
        private Long orderItemId;
        private String productName;
        private String sku;
        private Integer quantity;
        private Integer unitWeightGram;
        private BigDecimal unitPrice;
        private String category;
    }
}
