package com.vn.notification.web.notification.domain.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreatedEvent {
    private Long orderId;
    private String orderCode;
    private Long userId;
    private BigDecimal finalAmount;
    private LocalDateTime createdAt;
}
