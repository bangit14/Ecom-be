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
public class GhnSettingDto {

    @JsonProperty("shop_id")
    private Long shopId;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("note_code")
    @Builder.Default
    private String noteCode = "CHOTHUHANG"; // CHOTHUHANG, CHOXEMHANGKHONGTHU, KHONGCHOXEMHANG

    @JsonProperty("from_district_id")
    private Integer fromDistrictId;

    @JsonProperty("from_ward_code")
    private String fromWardCode;

    @JsonProperty("from_address")
    private String fromAddress;

    @JsonProperty("from_phone")
    private String fromPhone;

    @JsonProperty("from_name")
    private String fromName;

    @JsonProperty("default_service_type_id")
    @Builder.Default
    private Integer defaultServiceTypeId = 2; // 2 = Nhanh, 5 = Tiết kiệm / Hàng nặng

    @JsonProperty("default_payment_type_id")
    @Builder.Default
    private Integer defaultPaymentTypeId = 1; // 1 = Shop trả, 2 = Người nhận trả

    @JsonProperty("insurance_enabled")
    @Builder.Default
    private Boolean insuranceEnabled = true;
}
