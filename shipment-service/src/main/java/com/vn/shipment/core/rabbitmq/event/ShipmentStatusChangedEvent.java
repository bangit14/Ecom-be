package com.vn.shipment.core.rabbitmq.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStatusChangedEvent {

    private Long shipmentId;
    private Long orderId;
    private String shipmentCode;
    private String externalOrderCode;
    private String oldStatus;
    private String newStatus;
    private String trackingNumber;
    private String carrierName;
    private String note;
    private LocalDateTime updatedAt;
}
