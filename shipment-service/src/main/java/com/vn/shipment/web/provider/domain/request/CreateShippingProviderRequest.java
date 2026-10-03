package com.vn.shipment.web.provider.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShippingProviderRequest {

    @NotBlank(message = "Mã nhà vận chuyển không được để trống")
    private String code;

    @NotBlank(message = "Tên nhà vận chuyển không được để trống")
    private String name;

    private String logoUrl;

    @Builder.Default
    private Boolean supportsCod = true;

    @Builder.Default
    private Boolean supportsInsurance = false;

    @Builder.Default
    private Boolean supportsWebhook = true;

    private String trackingUrlTemplate;

    @Builder.Default
    private Boolean isActive = true;
}
