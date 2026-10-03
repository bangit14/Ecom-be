package com.vn.shipment.web.address.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingWardResponse {

    private Long id;
    private Long providerId;
    private Long districtRefId;
    private String externalCode;
    private String wardName;
    private String standardCode;
}
