package com.vn.shipment.web.provider.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShippingProviderRequest {

    private String name;
    private String logoUrl;
    private Boolean supportsCod;
    private Boolean supportsInsurance;
    private Boolean supportsWebhook;
    private String trackingUrlTemplate;
    private Boolean isActive;
}
