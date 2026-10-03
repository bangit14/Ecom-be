package com.vn.shipment.web.provider.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "shipping_provider_config", schema = "ecommerce_shipment")
public class ShippingProviderConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long providerId;
    private Long tenantId;
    private Long warehouseId;

    private String configName;

    @Builder.Default
    private String environment = "PRODUCTION";

    private String apiBaseUrl;
    private String token;
    private String apiKey;
    private String secretKey;
    private String clientId;

    private String providerShopId;
    private String providerAccountName;

    private String defaultPickupName;
    private String defaultPickupPhone;
    private String defaultPickupAddress;
    private String defaultPickupWard;
    private String defaultPickupDistrict;
    private String defaultPickupProvince;

    private String webhookUrl;
    private String webhookSecret;

    private Long defaultServiceId;

    @Builder.Default
    private String defaultPayer = "SHOP";

    @Builder.Default
    private Boolean defaultInsuranceEnabled = false;

    private Integer defaultWeightGram;

    private String extraSettings;

    @Builder.Default
    private Boolean isDefault = false;

    @Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
