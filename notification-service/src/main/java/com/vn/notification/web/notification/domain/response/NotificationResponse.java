package com.vn.notification.web.notification.domain.response;

import com.vn.notification.core.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private Long userId;
    private String title;
    private String body;
    private NotificationType type;
    private String actionUrl;
    private String imageUrl;
    private boolean isRead;
    private LocalDateTime createdAt;
}
