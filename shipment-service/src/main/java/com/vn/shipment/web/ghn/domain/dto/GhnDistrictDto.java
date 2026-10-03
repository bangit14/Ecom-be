package com.vn.shipment.web.ghn.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class GhnDistrictDto {

    @JsonProperty("DistrictID")
    private Integer districtId;

    @JsonProperty("ProvinceID")
    private Integer provinceId;

    @JsonProperty("DistrictName")
    private String districtName;

    @JsonProperty("Code")
    private String code;

    @JsonProperty("Type")
    private Integer type;

    @JsonProperty("SupportType")
    private Integer supportType;

    @JsonProperty("NameExtension")
    private List<String> nameExtension;

    @JsonProperty("CanUpdateCOD")
    private Boolean canUpdateCOD;

    @JsonProperty("Status")
    private Integer status;
}
