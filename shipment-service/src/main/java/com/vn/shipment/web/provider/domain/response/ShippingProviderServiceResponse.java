package com.vn.shipment.web.provider.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingProviderServiceResponse {

    private Long id;
    private Long providerId;
    private String serviceCode;
    private String serviceName;
    private BigDecimal maxWeightKg;
    private Boolean isActive;
}
