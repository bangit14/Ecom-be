package com.vn.shipment.web.shipment.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelShipmentRequest {

    @NotBlank(message = "Lý do hủy đơn vận chuyển không được để trống")
    private String cancelReason;
}
