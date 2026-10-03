package com.vn.shipment.web.shipment.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vn.shipment.core.enums.SyncSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "shipment_tracking", schema = "ecommerce_shipment")
public class ShipmentTracking {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shipmentId;
    private String trackingNumber;
    private String labelUrl;
    private String qrCodeUrl;
    private String currentStatus;
    private String currentStatusDescription;
    private LocalDateTime lastSyncedAt;

    @Builder.Default
    private String syncSource = SyncSource.WEBHOOK.name();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
