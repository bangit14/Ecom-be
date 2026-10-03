package com.vn.shipment.web.shipment.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vn.shipment.core.enums.EventSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "shipment_event", schema = "ecommerce_shipment")
public class ShipmentEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shipmentId;
    private Long trackingId;

    private String eventCode;
    private String eventStatus;
    private String eventDescription;
    private LocalDateTime eventTime;

    @Builder.Default
    private String source = EventSource.WEBHOOK.name();

    private String rawPayload;
    private LocalDateTime createdAt;
}
