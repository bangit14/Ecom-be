package com.vn.shipment.web.provider.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingProviderConfigResponse {

    private Long id;
    private Long providerId;
    private String providerCode;
    private String providerName;
    private Long tenantId;
    private Long warehouseId;
    private String configName;
    private String environment;
    private String apiBaseUrl;
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
    private String token;
    private Long defaultServiceId;
    private String defaultServiceCode;
    private String defaultServiceName;
    private String defaultPayer;
    private Boolean defaultInsuranceEnabled;
    private Integer defaultWeightGram;
    private String extraSettings;
    private Boolean isDefault;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
