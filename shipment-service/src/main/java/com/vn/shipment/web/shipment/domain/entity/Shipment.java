package com.vn.shipment.web.shipment.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vn.shipment.core.enums.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "shipment", schema = "ecommerce_shipment")
public class Shipment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long providerId;
    private Long providerConfigId;
    private Long serviceId;

    private String shipmentCode;
    private String externalOrderCode;

    @Builder.Default
    private String status = ShipmentStatus.PENDING.name();

    // Sender
    private String senderName;
    private String senderPhone;
    private String senderAddress;
    private String senderWard;
    private String senderDistrict;
    private String senderProvince;

    // Receiver
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String receiverWard;
    private String receiverDistrict;
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
    private LocalDateTime expectedDeliveryAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Builder.Default
    private List<ShipmentItem> items = new ArrayList<>();

    @TableField(exist = false)
    private ShipmentTracking tracking;

    @TableField(exist = false)
    @Builder.Default
    private List<ShipmentEvent> events = new ArrayList<>();
}
