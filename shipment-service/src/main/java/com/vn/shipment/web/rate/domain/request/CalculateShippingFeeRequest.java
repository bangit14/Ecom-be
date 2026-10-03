package com.vn.shipment.web.rate.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculateShippingFeeRequest {

    private String providerCode; // GHN, GHTK, optional
    private String serviceCode;  // optional

    @NotBlank(message = "Tỉnh/thành gửi không được để trống")
    private String fromProvince;
    private String fromDistrict;

    @NotBlank(message = "Tỉnh/thành nhận không được để trống")
    private String toProvince;
    private String toDistrict;

    @Builder.Default
    private Integer weightGram = 500;

    @Builder.Default
    private BigDecimal codAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal orderValue = BigDecimal.ZERO;
}
