package com.vn.order.web.order.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName(value = "order_histories", schema = "ecommerce_order")
public class OrderHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private OrderStatus status;
    private String note;
    private Long createdBy;
    private LocalDateTime createdAt;
}
