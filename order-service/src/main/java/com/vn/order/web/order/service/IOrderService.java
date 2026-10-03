package com.vn.order.web.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vn.order.core.enums.OrderStatus;
import com.vn.order.web.order.domain.entity.Order;
import com.vn.order.web.order.domain.request.CreateOrderRequest;
import com.vn.order.web.order.domain.request.OrderFilter;
import com.vn.order.web.order.domain.response.OrderResponse;
import com.vn.order.web.order.domain.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface IOrderService extends IService<Order> {

    PageResponse<OrderResponse> getMyOrders(Long userId, Pageable pageable);

    OrderResponse getMyOrderDetails(Long userId, Long orderId);

    OrderResponse createOrder(Long userId, CreateOrderRequest request);

    void cancelOrder(Long userId, Long orderId, String reason);

    PageResponse<OrderResponse> getAllOrders(OrderFilter filter, Pageable pageable);

    OrderResponse getOrderDetails(Long orderId);

    void updateOrderStatus(Long orderId, OrderStatus status, String note, Long staffId);
}
