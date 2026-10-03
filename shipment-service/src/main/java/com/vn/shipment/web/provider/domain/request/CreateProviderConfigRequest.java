package com.vn.shipment.web.provider.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProviderConfigRequest {

    @NotNull(message = "Provider ID không được để trống")
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
}
