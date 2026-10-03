package com.vn.notification.web.notification.domain.event;

import com.vn.notification.core.enums.NotificationType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {
    private Long userId;
    private String title;
    private String body;
    private NotificationType type;
    private String actionUrl;
    private String imageUrl;
}
