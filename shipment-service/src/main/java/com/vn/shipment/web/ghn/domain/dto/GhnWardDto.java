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
public class GhnWardDto {

    @JsonProperty("WardCode")
    private String wardCode;

    @JsonProperty("DistrictID")
    private Integer districtId;

    @JsonProperty("WardName")
    private String wardName;

    @JsonProperty("NameExtension")
    private List<String> nameExtension;

    @JsonProperty("CanUpdateCOD")
    private Boolean canUpdateCOD;

    @JsonProperty("SupportType")
    private Integer supportType;

    @JsonProperty("Status")
    private Integer status;
}
