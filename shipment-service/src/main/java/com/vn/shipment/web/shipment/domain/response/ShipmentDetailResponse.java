package com.vn.shipment.web.shipment.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentDetailResponse {

    private Long id;
    private Long orderId;
    private Long providerId;
    private String providerCode;
    private String providerName;
    private String providerLogoUrl;
    private Long providerConfigId;
    private Long serviceId;
    private String serviceCode;
    private String serviceName;

    private String shipmentCode;
    private String externalOrderCode;
    private String status;

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
    private LocalDateTime expectedDeliveryAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ShipmentTrackingResponse tracking;
    private List<ShipmentItemResponse> items;
    private List<ShipmentEventResponse> events;
}
