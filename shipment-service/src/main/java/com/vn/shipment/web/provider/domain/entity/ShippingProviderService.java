package com.vn.shipment.web.provider.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "shipping_provider_service", schema = "ecommerce_shipment")
public class ShippingProviderService {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long providerId;
    private String serviceCode;
    private String serviceName;
    private BigDecimal maxWeightKg;

    @Builder.Default
    private Boolean isActive = true;
}
