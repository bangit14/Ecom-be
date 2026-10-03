package com.vn.shipment.web.rate.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShippingRateRequest {

    @NotNull(message = "Provider ID không được để trống")
    private Long providerId;

    private Long serviceId;

    private String fromProvince;
    private String toProvince;
    private Integer weightFromGram;
    private Integer weightToGram;

    @NotNull(message = "Cước cơ bản không được để trống")
    private BigDecimal baseFee;

    @Builder.Default
    private BigDecimal feePerExtraKg = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal codFeePercent = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal insuranceFeePercent = BigDecimal.ZERO;

    @Builder.Default
    private String currency = "VND";

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    @Builder.Default
    private Boolean isActive = true;
}
