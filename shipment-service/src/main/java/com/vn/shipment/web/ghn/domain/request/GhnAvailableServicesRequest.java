package com.vn.shipment.web.ghn.domain.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GhnAvailableServicesRequest {

    @JsonProperty("shop_id")
    private Long shopId;

    @JsonProperty("from_district")
    @NotNull(message = "from_district không được để trống")
    private Integer fromDistrict;

    @JsonProperty("to_district")
    @NotNull(message = "to_district không được để trống")
    private Integer toDistrict;
}
