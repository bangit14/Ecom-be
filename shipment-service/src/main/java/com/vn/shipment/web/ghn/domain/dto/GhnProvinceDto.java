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
public class GhnProvinceDto {

    @JsonProperty("ProvinceID")
    private Integer provinceId;

    @JsonProperty("ProvinceName")
    private String provinceName;

    @JsonProperty("CountryID")
    private Integer countryId;

    @JsonProperty("Code")
    private String code;

    @JsonProperty("NameExtension")
    private List<String> nameExtension;

    @JsonProperty("IsEnable")
    private Integer isEnable;

    @JsonProperty("CanUpdateCOD")
    private Boolean canUpdateCOD;

    @JsonProperty("Status")
    private Integer status;
}
