package com.vn.shipment.web.provider.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveSellerShippingConfigRequest {

    @NotBlank(message = "Mã nhà vận chuyển không được để trống (GHN, GHTK, AHAMOVE, GOSHIP)")
    private String providerCode;

    private String configName;
    private String environment; // SANDBOX / PRODUCTION

    // Authentication / Credentials
    private String token;
    private String apiKey;
    private String secretKey;
    private String clientId;

    // Provider specific shop/partner identifier
    private String providerShopId;
    private String providerAccountName;

    // Default pickup address
    private String defaultPickupName;
    private String defaultPickupPhone;
    private String defaultPickupAddress;
    private String defaultPickupWard;
    private String defaultPickupDistrict;
    private String defaultPickupProvince;

    // Default order configurations
    private Long defaultServiceId;
    private String defaultServiceCode;

    @Builder.Default
    private String defaultPayer = "SHOP"; // SHOP, BUYER

    @Builder.Default
    private Boolean defaultInsuranceEnabled = false;

    @Builder.Default
    private Integer defaultWeightGram = 500;

    // Carrier specific JSON settings or raw DTO payload
    private GhnSettingDto ghnSettings;
    private GhtkSettingDto ghtkSettings;
    private AhamoveSettingDto ahamoveSettings;
    private GoShipSettingDto goshipSettings;
    private String extraSettingsJson;

    @Builder.Default
    private Boolean isDefault = false;

    @Builder.Default
    private Boolean isActive = true;
}
