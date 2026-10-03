package com.vn.order.web.outbox.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vn.order.core.enums.OutboxEventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "outbox_events", schema = "ecommerce_order")
public class OutboxEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String topic;
    private String kafkaKey;
    private String payloadClass;
    private String payload;

    @Builder.Default
    private OutboxEventStatus status = OutboxEventStatus.PENDING;

    @Builder.Default
    private Integer retryCount = 0;

    @Builder.Default
    private Integer maxRetries = 5;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
