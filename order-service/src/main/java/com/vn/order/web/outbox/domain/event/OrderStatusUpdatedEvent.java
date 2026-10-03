package com.vn.order.web.outbox.domain.event;

import com.vn.order.core.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdatedEvent {
    private Long orderId;
    private String orderCode;
    private Long userId;
    private OrderStatus oldStatus;
    private OrderStatus newStatus;
    private String note;
    private LocalDateTime updatedAt;
}
