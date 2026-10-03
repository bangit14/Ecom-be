package com.vn.shipment.web.shipment.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentFilterRequest {

    private Long orderId;
    private Long providerId;
    private String status;
    private String shipmentCode;
    private String trackingNumber;
    private String receiverPhone;

    @Builder.Default
    private Integer page = 1;

    @Builder.Default
    private Integer size = 20;
}
