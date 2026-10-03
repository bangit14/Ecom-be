package com.vn.shipment.web.provider.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProviderServiceRequest {

    @NotNull(message = "Provider ID không được để trống")
    private Long providerId;

    @NotBlank(message = "Mã gói dịch vụ không được để trống")
    private String serviceCode;

    @NotBlank(message = "Tên gói dịch vụ không được để trống")
    private String serviceName;

    private BigDecimal maxWeightKg;

    @Builder.Default
    private Boolean isActive = true;
}
