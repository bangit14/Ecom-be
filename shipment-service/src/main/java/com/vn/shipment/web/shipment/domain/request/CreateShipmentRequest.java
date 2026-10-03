package com.vn.shipment.web.shipment.domain.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShipmentRequest {

    @NotNull(message = "Order ID không được để trống")
    private Long orderId;

    private String orderCode;

    private Long providerId;
    private String providerCode; // GHN, GHTK, AHAMOVE, GOSHIP
    private Long providerConfigId;
    private Long serviceId;
    private String serviceCode;

    // Sender
    private String senderName;
    private String senderPhone;
    private String senderAddress;
    private String senderWard;
    private String senderDistrict;
    private String senderProvince;

    // Receiver
    @NotBlank(message = "Tên người nhận không được để trống")
    private String receiverName;

    @NotBlank(message = "Số điện thoại người nhận không được để trống")
    private String receiverPhone;

    @NotBlank(message = "Địa chỉ người nhận không được để trống")
    private String receiverAddress;

    private String receiverWard;
    private String receiverDistrict;

    @NotBlank(message = "Tỉnh/thành người nhận không được để trống")
    private String receiverProvince;

    @Builder.Default
    private BigDecimal codAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal insuranceAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    private Integer totalWeightGram;
    private BigDecimal totalValue;
    private String note;

    @NotEmpty(message = "Danh sách hàng hóa trong đơn không được để trống")
    @Valid
    private List<ShipmentItemRequest> items;
}
