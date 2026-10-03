package com.vn.shipment.web.provider.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AhamoveSettingDto {

    @JsonProperty("mobile")
    private String mobile; // Merchant phone number

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("request_source")
    @Builder.Default
    private String requestSource = "THIRD_PARTY";

    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("default_service_id")
    @Builder.Default
    private String defaultServiceId = "SGN-BIKE"; // SGN-BIKE, SGN-TRUCK500, SGN-TRUCK1000

    @JsonProperty("payment_method")
    @Builder.Default
    private String paymentMethod = "CASH"; // CASH, MOMO, BALANCE
}
