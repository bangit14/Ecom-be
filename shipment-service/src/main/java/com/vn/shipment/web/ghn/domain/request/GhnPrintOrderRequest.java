package com.vn.shipment.web.ghn.domain.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vn.shipment.core.enums.GhnPrintSize;
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
public class GhnPrintOrderRequest {

    @JsonProperty("order_codes")
    @NotEmpty(message = "order_codes không được để trống")
    private List<String> orderCodes;

    @JsonProperty("print_size")
    @Builder.Default
    private GhnPrintSize printSize = GhnPrintSize.A5; // A5, SIZE_80x80, SIZE_52x70
}
