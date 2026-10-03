package com.vn.shipment.web.address.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingProvinceResponse {

    private Long id;
    private Long providerId;
    private String externalCode;
    private String provinceName;
    private String standardCode;
}
