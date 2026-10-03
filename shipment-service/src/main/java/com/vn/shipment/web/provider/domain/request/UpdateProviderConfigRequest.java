package com.vn.shipment.web.provider.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProviderConfigRequest {

    private Long tenantId;
    private Long warehouseId;
    private String configName;
    private String environment;
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
    private String defaultPayer;
    private Boolean defaultInsuranceEnabled;
    private Integer defaultWeightGram;
    private String extraSettings;
    private Boolean isDefault;
    private Boolean isActive;
}
