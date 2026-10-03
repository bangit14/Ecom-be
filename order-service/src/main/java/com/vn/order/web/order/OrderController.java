package com.vn.order.web.order;

import com.vn.order.core.enums.OrderStatus;
import com.vn.order.core.exception.AppException;
import com.vn.order.core.exception.ErrorCode;
import com.vn.order.core.security.UserContext;
import com.vn.order.core.util.PageableUtils;
import com.vn.order.web.order.domain.request.CreateOrderRequest;
import com.vn.order.web.order.domain.request.OrderFilter;
import com.vn.order.web.order.domain.response.OrderResponse;
import com.vn.order.web.order.domain.response.PageResponse;
import com.vn.order.web.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j(topic = "Order-Controller")
public class OrderController {

    private final IOrderService orderService;

    private Long requireUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Yêu cầu đăng nhập để thực hiện");
        }
        return userId;
    }

    @GetMapping("/my")
    public ResponseEntity<PageResponse<OrderResponse>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, sortDir);
        return ResponseEntity.ok(orderService.getMyOrders(requireUserId(), pageable));
    }

    @GetMapping("/my/{id}")
    public ResponseEntity<OrderResponse> getMyOrderDetails(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getMyOrderDetails(requireUserId(), id));
    }

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(requireUserId(), request));
    }

    @PutMapping("/{id}/update-status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            @RequestParam(required = false) String note) {
        Long staffId = UserContext.getUserId() != null ? UserContext.getUserId() : 0L;
        orderService.updateOrderStatus(id, status, note, staffId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Khách hàng hủy đơn") String reason) {
        orderService.cancelOrder(requireUserId(), id, reason);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<PageResponse<OrderResponse>> getAllOrders(
            @ModelAttribute OrderFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, sortDir);
        return ResponseEntity.ok(orderService.getAllOrders(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderDetails(id));
    }
}
