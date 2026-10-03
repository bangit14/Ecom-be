package com.vn.shipment.web.ghn.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vn.shipment.web.ghn.domain.dto.GhnFeeDetailDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GhnCreateOrderResponse {

    @JsonProperty("order_code")
    private String orderCode;

    @JsonProperty("sort_code")
    private String sortCode;

    @JsonProperty("trans_type")
    private String transType;

    @JsonProperty("ward_encode")
    private String wardEncode;

    @JsonProperty("district_encode")
    private String districtEncode;

    @JsonProperty("fee")
    private GhnFeeDetailDto fee;

    @JsonProperty("total_fee")
    private Long totalFee;

    @JsonProperty("expected_delivery_time")
    private String expectedDeliveryTime;
}
