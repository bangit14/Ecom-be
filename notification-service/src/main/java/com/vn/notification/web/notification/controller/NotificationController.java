package com.vn.notification.web.notification.controller;

import com.vn.notification.core.security.UserContext;
import com.vn.notification.web.notification.domain.request.SendNotificationRequest;
import com.vn.notification.web.notification.domain.response.NotificationResponse;
import com.vn.notification.web.notification.domain.response.PageResponse;
import com.vn.notification.web.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** Lấy danh sách thông báo của tôi (phân trang) */
    @GetMapping("/my")
    public ResponseEntity<PageResponse<NotificationResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(notificationService.getMyNotifications(userId, pageable));
    }

    /** Số thông báo chưa đọc */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    /** Đánh dấu 1 thông báo đã đọc */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificationService.markAsRead(id, userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /** Đánh dấu tất cả thông báo đã đọc */
    @PutMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificationService.markAllAsRead(userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /** Internal API (gọi từ service khác): Gửi thông báo đến user */
    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(
            @RequestParam Long userId,
            @RequestBody SendNotificationRequest request) {
        notificationService.sendNotification(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private Long getCurrentUserId() {
        return UserContext.getUserId();
    }
}
