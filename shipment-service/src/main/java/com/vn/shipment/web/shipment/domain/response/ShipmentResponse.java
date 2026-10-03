package com.vn.shipment.web.shipment.domain.response;

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
public class ShipmentResponse {

    private Long id;
    private Long orderId;
    private Long providerId;
    private String providerCode;
    private String providerName;
    private String providerLogoUrl;
    private Long serviceId;
    private String serviceName;

    private String shipmentCode;
    private String externalOrderCode;
    private String status;

    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String receiverProvince;

    private BigDecimal codAmount;
    private BigDecimal shippingFee;
    private Integer totalWeightGram;
    private BigDecimal totalValue;

    private String trackingNumber;
    private String trackingUrl;

    private LocalDateTime expectedDeliveryAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
