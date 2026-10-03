package com.vn.notification.web.notification.service.impl;

import com.vn.notification.core.enums.NotificationType;
import com.vn.notification.web.notification.domain.entity.Notification;
import com.vn.notification.web.notification.domain.request.SendNotificationRequest;
import com.vn.notification.web.notification.domain.response.NotificationResponse;
import com.vn.notification.web.notification.domain.response.PageResponse;
import com.vn.notification.web.notification.repository.NotificationRepository;
import com.vn.notification.web.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "NotificationService")
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getMyNotifications(Long userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return PageResponse.<NotificationResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long id, Long userId) {
        notificationRepository.findByIdAndUserId(id, userId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional
    public void sendNotification(Long userId, SendNotificationRequest request) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(request.getTitle())
                .body(request.getBody())
                .type(request.getType() != null ? request.getType() : NotificationType.SYSTEM)
                .actionUrl(request.getActionUrl())
                .imageUrl(request.getImageUrl())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.info("Saved notification for user: {}", userId);
    }

    @Override
    @Transactional
    public void createOrderNotification(Long userId, String orderCode, String status) {
        String title = "Đơn hàng #" + orderCode;
        String body = "Đơn hàng của bạn đã được " + status;

        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .body(body)
                .type(NotificationType.ORDER)
                .actionUrl("/orders/" + orderCode)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.info("Saved order notification for user {} (order: {})", userId, orderCode);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .type(notification.getType())
                .actionUrl(notification.getActionUrl())
                .imageUrl(notification.getImageUrl())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
