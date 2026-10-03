package com.vn.shipment.web.ghn.domain.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
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
public class GhnCancelOrderRequest {

    @JsonProperty("order_codes")
    @NotEmpty(message = "order_codes không được để trống")
    private List<String> orderCodes;

    @JsonProperty("reason_code")
    @Builder.Default
    private String reasonCode = "GHN-CANCEL-OTHER";

    @JsonProperty("reason")
    private String reason;
}
