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
public class GhtkSettingDto {

    @JsonProperty("partner_code")
    private String partnerCode;

    @JsonProperty("pick_address_id")
    private String pickAddressId;

    @JsonProperty("default_deliver_option")
    @Builder.Default
    private String defaultDeliverOption = "none"; // xteam, none

    @JsonProperty("is_freeship")
    @Builder.Default
    private Integer isFreeship = 0; // 0 = Khách trả, 1 = Shop freeship

    @JsonProperty("use_return_address")
    @Builder.Default
    private Boolean useReturnAddress = false;

    @JsonProperty("return_address")
    private String returnAddress;

    @JsonProperty("return_phone")
    private String returnPhone;

    @JsonProperty("return_name")
    private String returnName;
}
