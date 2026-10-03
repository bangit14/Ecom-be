package com.vn.shipment.web.address.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingDistrictResponse {

    private Long id;
    private Long providerId;
    private Long provinceRefId;
    private String externalCode;
    private String districtName;
    private String standardCode;
}
