package com.vn.shipment.web.provider.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingProviderResponse {

    private Long id;
    private String code;
    private String name;
    private String logoUrl;
    private Boolean supportsCod;
    private Boolean supportsInsurance;
    private Boolean supportsWebhook;
    private String trackingUrlTemplate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<ShippingProviderServiceResponse> services;
}
