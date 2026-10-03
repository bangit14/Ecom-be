package com.vn.shipment.web.rate.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName(value = "shipping_rate", schema = "ecommerce_shipment")
public class ShippingRate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long providerId;
    private Long serviceId;

    private String fromProvince;
    private String toProvince;
    private Integer weightFromGram;
    private Integer weightToGram;

    @Builder.Default
    private BigDecimal baseFee = BigDecimal.ZERO;

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

    private LocalDateTime createdAt;
}
