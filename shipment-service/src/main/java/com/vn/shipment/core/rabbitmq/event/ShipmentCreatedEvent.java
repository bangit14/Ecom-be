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
public class ShipmentCreatedEvent {

    private Long shipmentId;
    private Long orderId;
    private String shipmentCode;
    private String externalOrderCode;
    private String status;
    private String trackingNumber;
    private LocalDateTime createdAt;
}
