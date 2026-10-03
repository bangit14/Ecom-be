package com.vn.notification.web.notification.domain.request;

import com.vn.notification.core.enums.NotificationType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendNotificationRequest {
    private String title;
    private String body;
    private NotificationType type;
    private String actionUrl;
    private String imageUrl;
}
