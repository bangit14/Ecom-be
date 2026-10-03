package com.vn.shipment.web.rate.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingFeeEstimateResponse {

    private String providerCode;
    private String providerName;
    private String serviceCode;
    private String serviceName;
    private String logoUrl;

    private BigDecimal shippingFee;
    private BigDecimal baseFee;
    private BigDecimal extraWeightFee;
    private BigDecimal codFee;
    private BigDecimal insuranceFee;
    private BigDecimal totalFee;

    private String estimatedDeliveryTime; // vd: "1-2 ngày"
    private String currency;
}
