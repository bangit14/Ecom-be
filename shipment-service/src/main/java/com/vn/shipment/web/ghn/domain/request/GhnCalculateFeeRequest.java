package com.vn.shipment.web.ghn.domain.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vn.shipment.web.ghn.domain.dto.GhnOrderItemDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GhnCalculateFeeRequest {

    @JsonProperty("from_district_id")
    private Integer fromDistrictId;

    @JsonProperty("from_ward_code")
    private String fromWardCode;

    @JsonProperty("service_id")
    private Integer serviceId;

    @JsonProperty("service_type_id")
    @Builder.Default
    private Integer serviceTypeId = 2;

    @JsonProperty("to_district_id")
    @NotNull(message = "to_district_id không được để trống")
    private Integer toDistrictId;

    @JsonProperty("to_ward_code")
    private String toWardCode;

    @JsonProperty("weight")
    @NotNull(message = "weight không được để trống (gram)")
    private Integer weight;

    @JsonProperty("length")
    private Integer length;

    @JsonProperty("width")
    private Integer width;

    @JsonProperty("height")
    private Integer height;

    @JsonProperty("insurance_value")
    @Builder.Default
    private Long insuranceValue = 0L;

    @JsonProperty("coupon")
    private String coupon;

    @JsonProperty("cod_failed_amount")
    @Builder.Default
    private Long codFailedAmount = 0L;

    @JsonProperty("items")
    private List<GhnOrderItemDto> items;
}
