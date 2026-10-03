package com.vn.shipment.web.shipment.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShipmentStatusRequest {

    @NotBlank(message = "Trạng thái mới không được để trống")
    private String status; // PENDING, CONFIRMED, READY_TO_PICK, PICKING, IN_TRANSIT, DELIVERED, DELIVERY_FAILED, RETURNED, CANCELLED

    private String trackingNumber;
    private String statusDescription;
    private String location;
    private String note;
    private LocalDateTime eventTime;
}
