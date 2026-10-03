package com.vn.order.web.order.domain.response;

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
public class OrderHistoryResponse {
    private Long id;
    private OrderStatus status;
    private String note;
    private Long createdBy;
    private LocalDateTime createdAt;
}
