package com.vn.notification.web.notification.domain.event;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdatedEvent {
    private Long orderId;
    private String orderCode;
    private Long userId;
    private String oldStatus;
    private String newStatus;
    private String note;
    private LocalDateTime updatedAt;
}
