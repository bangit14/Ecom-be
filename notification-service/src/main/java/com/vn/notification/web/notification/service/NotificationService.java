package com.vn.notification.web.notification.service;

import com.vn.notification.web.notification.domain.request.SendNotificationRequest;
import com.vn.notification.web.notification.domain.response.NotificationResponse;
import com.vn.notification.web.notification.domain.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    PageResponse<NotificationResponse> getMyNotifications(Long userId, Pageable pageable);

    long getUnreadCount(Long userId);

    void markAsRead(Long id, Long userId);

    void markAllAsRead(Long userId);

    void sendNotification(Long userId, SendNotificationRequest request);

    void createOrderNotification(Long userId, String orderCode, String status);
}
