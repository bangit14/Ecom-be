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
public class GhnServicePackageDto {

    @JsonProperty("service_id")
    private Integer serviceId;

    @JsonProperty("short_name")
    private String shortName;

    @JsonProperty("service_type_id")
    private Integer serviceTypeId;

    @JsonProperty("config_fee_id")
    private String configFeeId;

    @JsonProperty("extra_cost_id")
    private String extraCostId;

    @JsonProperty("standard_config_fee_id")
    private String standardConfigFeeId;
}
