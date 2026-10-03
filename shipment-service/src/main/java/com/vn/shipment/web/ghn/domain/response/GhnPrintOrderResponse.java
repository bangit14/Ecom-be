package com.vn.shipment.web.ghn.domain.response;

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
public class GhnPrintOrderResponse {

    @JsonProperty("token")
    private String token;

    @JsonProperty("print_url")
    private String printUrl;

    @JsonProperty("order_codes")
    private List<String> orderCodes;
}
