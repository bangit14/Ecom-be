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
public class ShipmentTrackingResponse {

    private Long id;
    private Long shipmentId;
    private String trackingNumber;
    private String labelUrl;
    private String qrCodeUrl;
    private String currentStatus;
    private String currentStatusDescription;
    private LocalDateTime lastSyncedAt;
    private String syncSource;
    private String trackingUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
