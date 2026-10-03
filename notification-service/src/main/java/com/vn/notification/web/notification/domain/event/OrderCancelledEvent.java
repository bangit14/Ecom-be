package com.vn.notification.web.notification.domain.event;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCancelledEvent {
    private Long orderId;
    private String orderCode;
    private Long userId;
    private String reason;
    private LocalDateTime cancelledAt;
}
