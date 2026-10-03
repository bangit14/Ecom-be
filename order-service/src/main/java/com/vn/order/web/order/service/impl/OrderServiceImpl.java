package com.vn.order.web.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vn.order.core.client.AuthClient;
import com.vn.order.core.client.ProductClient;
import com.vn.order.core.enums.OrderStatus;
import com.vn.order.core.exception.AppException;
import com.vn.order.core.exception.ErrorCode;
import com.vn.order.web.cart.domain.entity.Cart;
import com.vn.order.web.cart.domain.entity.CartItem;
import com.vn.order.web.cart.service.ICartService;
import com.vn.order.web.order.domain.entity.Order;
import com.vn.order.web.order.domain.entity.OrderHistory;
import com.vn.order.web.order.domain.entity.OrderItem;
import com.vn.order.web.order.domain.request.CreateOrderRequest;
import com.vn.order.web.order.domain.request.OrderFilter;
import com.vn.order.web.order.domain.request.SelectedCartItem;
import com.vn.order.web.order.domain.response.*;
import com.vn.order.web.order.mapper.OrderHistoryMapper;
import com.vn.order.web.order.mapper.OrderItemMapper;
import com.vn.order.web.order.mapper.OrderMapper;
import com.vn.order.web.order.service.IOrderService;
import com.vn.order.web.order.service.OrderCodeGenerator;
import com.vn.order.web.outbox.domain.event.OrderCancelledEvent;
import com.vn.order.web.outbox.domain.event.OrderCreatedEvent;
import com.vn.order.web.outbox.domain.event.OrderStatusUpdatedEvent;
import com.vn.order.web.outbox.service.IOutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "Order-Service")
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final OrderItemMapper orderItemMapper;
    private final OrderHistoryMapper orderHistoryMapper;
    private final ICartService cartService;
    private final ProductClient productClient;
    private final AuthClient authClient;
    private final IOutboxEventService outboxEventService;
    private final OrderCodeGenerator orderCodeGenerator;

    @Override
    public PageResponse<OrderResponse> getMyOrders(Long userId, Pageable pageable) {
        Page<Order> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreatedAt);

        IPage<Order> result = baseMapper.selectPage(page, wrapper);
        return buildPageResponse(result);
    }

    @Override
    public OrderResponse getMyOrderDetails(Long userId, Long orderId) {
        Order order = baseMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        List<OrderHistory> histories = orderHistoryMapper.selectList(
                new LambdaQueryWrapper<OrderHistory>().eq(OrderHistory::getOrderId, orderId).orderByDesc(OrderHistory::getCreatedAt));

        return mapToOrderResponse(order, items, histories);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        if (request.getSelectedItems() == null || request.getSelectedItems().isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY, "Vui lòng chọn ít nhất một sản phẩm để đặt hàng");
        }

        Cart cart = cartService.getCartDomain(userId);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY, "Giỏ hàng của bạn đang trống");
        }

        List<CartItem> selectedCartItems = filterSelectedItems(cart.getItems(), request.getSelectedItems());
        if (selectedCartItems.isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY, "Không tìm thấy sản phẩm đã chọn trong giỏ hàng");
        }

        // 1. Reserve stock atomically across all items
        List<CartItem> reservedItems = new ArrayList<>();
        try {
            for (CartItem item : selectedCartItems) {
                productClient.reserveStock(item.getVariantId(), item.getQuantity());
                reservedItems.add(item);
            }
        } catch (Exception e) {
            // Rollback already reserved stock
            for (CartItem reserved : reservedItems) {
                productClient.releaseStock(reserved.getVariantId(), reserved.getQuantity());
            }
            throw e;
        }

        // 2. Fetch shipping address from auth-service
        AddressResponse address;
        if (request.getAddressId() != null) {
            address = authClient.getAddress(request.getAddressId());
        } else {
            address = authClient.getDefaultAddress(userId);
        }

        // 3. Fetch user summary for snapshot
        UserSummaryResponse userSummary = authClient.getUser(userId);

        // 4. Build and persist Order
        Order order = Order.builder()
                .orderCode(orderCodeGenerator.generateOrderCode())
                .userId(userId)
                .customerName(userSummary != null ? userSummary.getFullName() : (address != null ? address.getFullName() : "Khách hàng"))
                .customerEmail(userSummary != null ? userSummary.getEmail() : "")
                .customerPhone(address != null ? address.getPhone() : (userSummary != null ? userSummary.getPhone() : ""))
                .status(OrderStatus.PENDING)
                .note(request.getNote())
                .shippingAddressId(address != null ? address.getId() : null)
                .shippingFullName(address != null ? address.getFullName() : "")
                .shippingPhone(address != null ? address.getPhone() : "")
                .shippingAddressLine(address != null ? address.getAddressLine() : "")
                .shippingWard(address != null ? address.getWard() : "")
                .shippingDistrict(address != null ? address.getDistrict() : "")
                .shippingProvince(address != null ? address.getProvince() : "")
                .shippingCountry(address != null ? address.getCountry() : "")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : selectedCartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .productId(item.getProductId())
                    .variantId(item.getVariantId())
                    .sku(item.getSku())
                    .productName(item.getProductName())
                    .variantAttributes(item.getVariantAttributes())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getPrice())
                    .subtotal(item.getSubtotal())
                    .build();
            orderItems.add(orderItem);
            order.addItem(orderItem);
        }

        order.calculateTotals();
        baseMapper.insert(order);

        // Insert items with generated orderId
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        // Insert initial history
        OrderHistory initialHistory = OrderHistory.builder()
                .orderId(order.getId())
                .status(OrderStatus.PENDING)
                .note("Đơn hàng đã được khởi tạo")
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .build();
        orderHistoryMapper.insert(initialHistory);

        // 5. Emit Outbox Event for Kafka
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUserId())
                .finalAmount(order.getFinalAmount())
                .createdAt(order.getCreatedAt())
                .build();

        outboxEventService.saveOutboxEvent(
                "ORDER", order.getOrderCode(),
                "order-created", "order-created", order.getOrderCode(), event
        );

        // 6. Remove ordered items from Redis cart after commit
        Set<Long> variantIdsToRemove = selectedCartItems.stream()
                .map(CartItem::getVariantId)
                .collect(Collectors.toSet());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cartService.removeItems(userId, variantIdsToRemove);
                log.info("Removed {} items from cart for user {}", variantIdsToRemove.size(), userId);
            }
        });

        log.info("Order {} (code: {}) successfully created for user {}", order.getId(), order.getOrderCode(), userId);
        return mapToOrderResponse(order, orderItems, List.of(initialHistory));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId, String reason) {
        Order order = baseMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId));

        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        if (!order.isCancellable()) {
            throw new AppException(ErrorCode.ORDER_CANNOT_CANCEL, "Không thể hủy đơn hàng ở trạng thái: " + order.getStatus());
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));

        // Release reserved stock back to product-service
        for (OrderItem item : items) {
            if (item.getVariantId() != null) {
                productClient.releaseStock(item.getVariantId(), item.getQuantity());
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(order);

        OrderHistory history = OrderHistory.builder()
                .orderId(order.getId())
                .status(OrderStatus.CANCELLED)
                .note(StringUtils.hasText(reason) ? reason : "Khách hàng hủy đơn")
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .build();
        orderHistoryMapper.insert(history);

        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUserId())
                .reason(reason)
                .cancelledAt(LocalDateTime.now())
                .build();

        outboxEventService.saveOutboxEvent(
                "ORDER", order.getOrderCode(),
                "order-cancelled", "order-cancelled", order.getOrderCode(), event
        );

        log.info("Order {} cancelled by user {}. Reason: {}", orderId, userId, reason);
    }

    @Override
    public PageResponse<OrderResponse> getAllOrders(OrderFilter filter, Pageable pageable) {
        Page<Order> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (filter != null) {
            if (StringUtils.hasText(filter.getOrderCode())) {
                wrapper.like(Order::getOrderCode, filter.getOrderCode());
            }
            if (filter.getOrderStatus() != null) {
                wrapper.eq(Order::getStatus, filter.getOrderStatus());
            }
            if (filter.getPaymentStatus() != null) {
                wrapper.eq(Order::getPaymentStatus, filter.getPaymentStatus());
            }
            if (filter.getOrderDateFrom() != null) {
                wrapper.ge(Order::getCreatedAt, filter.getOrderDateFrom());
            }
            if (filter.getOrderDateTo() != null) {
                wrapper.le(Order::getCreatedAt, filter.getOrderDateTo());
            }
            if ("asc".equalsIgnoreCase(filter.getSortDir())) {
                wrapper.orderByAsc(Order::getCreatedAt);
            } else {
                wrapper.orderByDesc(Order::getCreatedAt);
            }
        } else {
            wrapper.orderByDesc(Order::getCreatedAt);
        }

        IPage<Order> result = baseMapper.selectPage(page, wrapper);
        return buildPageResponse(result);
    }

    @Override
    public OrderResponse getOrderDetails(Long orderId) {
        Order order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        List<OrderHistory> histories = orderHistoryMapper.selectList(
                new LambdaQueryWrapper<OrderHistory>().eq(OrderHistory::getOrderId, orderId).orderByDesc(OrderHistory::getCreatedAt));

        return mapToOrderResponse(order, items, histories);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Long orderId, OrderStatus newStatus, String note, Long staffId) {
        Order order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        OrderStatus oldStatus = order.getStatus();

        // If transitioning to CANCELLED, release stock back
        if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
            for (OrderItem item : items) {
                if (item.getVariantId() != null) {
                    productClient.releaseStock(item.getVariantId(), item.getQuantity());
                }
            }
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(order);

        OrderHistory history = OrderHistory.builder()
                .orderId(order.getId())
                .status(newStatus)
                .note(note != null ? note : ("Chuyển trạng thái sang " + newStatus.getDescription()))
                .createdBy(staffId)
                .createdAt(LocalDateTime.now())
                .build();
        orderHistoryMapper.insert(history);

        OrderStatusUpdatedEvent event = OrderStatusUpdatedEvent.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUserId())
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .note(note)
                .updatedAt(LocalDateTime.now())
                .build();

        outboxEventService.saveOutboxEvent(
                "ORDER", order.getOrderCode(),
                "order-status-updated", "order-status-updated", order.getOrderCode(), event
        );

        log.info("Order {} status updated from {} to {} by staff {}", orderId, oldStatus, newStatus, staffId);
    }

    private PageResponse<OrderResponse> buildPageResponse(IPage<Order> result) {
        List<Order> orders = result.getRecords();
        if (orders == null || orders.isEmpty()) {
            return PageResponse.<OrderResponse>builder()
                    .content(Collections.emptyList())
                    .pageNumber((int) result.getCurrent())
                    .pageSize((int) result.getSize())
                    .totalElements(result.getTotal())
                    .totalPages((int) result.getPages())
                    .last(true)
                    .first(true)
                    .empty(true)
                    .build();
        }

        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        List<OrderItem> allItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));
        List<OrderHistory> allHistories = orderHistoryMapper.selectList(
                new LambdaQueryWrapper<OrderHistory>().in(OrderHistory::getOrderId, orderIds).orderByDesc(OrderHistory::getCreatedAt));

        Map<Long, List<OrderItem>> itemsByOrderId = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
        Map<Long, List<OrderHistory>> historiesByOrderId = allHistories.stream()
                .collect(Collectors.groupingBy(OrderHistory::getOrderId));

        List<OrderResponse> content = orders.stream()
                .map(order -> mapToOrderResponse(
                        order,
                        itemsByOrderId.getOrDefault(order.getId(), Collections.emptyList()),
                        historiesByOrderId.getOrDefault(order.getId(), Collections.emptyList())))
                .toList();

        return PageResponse.<OrderResponse>builder()
                .content(content)
                .pageNumber((int) result.getCurrent())
                .pageSize((int) result.getSize())
                .totalElements(result.getTotal())
                .totalPages((int) result.getPages())
                .last(result.getCurrent() >= result.getPages())
                .first(result.getCurrent() <= 1)
                .empty(content.isEmpty())
                .build();
    }

    private List<CartItem> filterSelectedItems(List<CartItem> cartItems, List<SelectedCartItem> selected) {
        Set<Long> selectedVariantIds = selected.stream()
                .map(SelectedCartItem::getVariantId)
                .collect(Collectors.toSet());

        return cartItems.stream()
                .filter(item -> selectedVariantIds.contains(item.getVariantId()))
                .toList();
    }

    private OrderResponse mapToOrderResponse(Order order, List<OrderItem> items, List<OrderHistory> histories) {
        AddressResponse addressResponse = AddressResponse.builder()
                .id(order.getShippingAddressId())
                .fullName(order.getShippingFullName())
                .phone(order.getShippingPhone())
                .addressLine(order.getShippingAddressLine())
                .ward(order.getShippingWard())
                .district(order.getShippingDistrict())
                .province(order.getShippingProvince())
                .country(order.getShippingCountry())
                .build();

        List<OrderItemResponse> itemResponses = items.stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .variantId(item.getVariantId())
                        .sku(item.getSku())
                        .productName(item.getProductName())
                        .variantAttributes(item.getVariantAttributes())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subTotal(item.getSubtotal())
                        .build())
                .toList();

        List<OrderHistoryResponse> historyResponses = histories.stream()
                .map(h -> OrderHistoryResponse.builder()
                        .id(h.getId())
                        .status(h.getStatus())
                        .note(h.getNote())
                        .createdBy(h.getCreatedBy())
                        .createdAt(h.getCreatedAt())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUserId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .customerPhone(order.getCustomerPhone())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .shippingFee(order.getShippingFee())
                .finalAmount(order.getFinalAmount())
                .paidAmount(order.getPaidAmount())
                .refundedAmount(order.getRefundedAmount())
                .note(order.getNote())
                .shippingAddress(addressResponse)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemResponses)
                .histories(historyResponses)
                .build();
    }
}
