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
public class GoShipSettingDto {

    @JsonProperty("partner_id")
    private String partnerId;

    @JsonProperty("partner_key")
    private String partnerKey;

    @JsonProperty("warehouse_code")
    private String warehouseCode;

    @JsonProperty("default_service_code")
    @Builder.Default
    private String defaultServiceCode = "GOSHIP_ECONOMY";
}
