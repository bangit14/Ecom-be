package com.vn.shipment.web.rate.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingRateResponse {

    private Long id;
    private Long providerId;
    private String providerCode;
    private String providerName;
    private Long serviceId;
    private String serviceCode;
    private String serviceName;

    private String fromProvince;
    private String toProvince;
    private Integer weightFromGram;
    private Integer weightToGram;

    private BigDecimal baseFee;
    private BigDecimal feePerExtraKg;
    private BigDecimal codFeePercent;
    private BigDecimal insuranceFeePercent;
    private String currency;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
