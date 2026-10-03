package com.vn.shipment.web.shipment.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentEventResponse {

    private Long id;
    private Long shipmentId;
    private Long trackingId;
    private String eventCode;
    private String eventStatus;
    private String eventDescription;
    private LocalDateTime eventTime;
    private String source;
    private String rawPayload;
    private LocalDateTime createdAt;
}
