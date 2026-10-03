package com.vn.shipment.web.ghn.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GhnFeeDetailDto {

    @JsonProperty("main_service")
    private Long mainService;

    @JsonProperty("insurance")
    private Long insurance;

    @JsonProperty("cod_fee")
    private Long codFee;

    @JsonProperty("station_do")
    private Long stationDo;

    @JsonProperty("station_pu")
    private Long stationPu;

    @JsonProperty("return")
    private Long returnFee;

    @JsonProperty("r2s")
    private Long r2s;

    @JsonProperty("return_again")
    private Long returnAgain;

    @JsonProperty("coupon")
    private Long coupon;

    @JsonProperty("document_return")
    private Long documentReturn;

    @JsonProperty("double_check")
    private Long doubleCheck;

    @JsonProperty("double_check_deliver")
    private Long doubleCheckDeliver;

    @JsonProperty("pick_remote_areas_fee")
    private Long pickRemoteAreasFee;

    @JsonProperty("deliver_remote_areas_fee")
    private Long deliverRemoteAreasFee;

    @JsonProperty("pick_remote_areas_fee_return")
    private Long pickRemoteAreasFeeReturn;

    @JsonProperty("deliver_remote_areas_fee_return")
    private Long deliverRemoteAreasFeeReturn;

    @JsonProperty("cod_failed_fee")
    private Long codFailedFee;

    @JsonProperty("change_to_address_fee")
    private Long changeToAddressFee;

    @JsonProperty("change_return_address_fee")
    private Long changeReturnAddressFee;
}
