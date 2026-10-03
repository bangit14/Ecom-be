package com.vn.notification.web.notification.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.notification.core.enums.NotificationType;
import com.vn.notification.web.notification.domain.request.SendNotificationRequest;
import com.vn.notification.web.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "NotificationConsumer")
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-created", groupId = "notification-service-group")
    public void handleOrderCreated(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            JsonNode json = parseToJsonNode(record.value());
            Long userId = json.has("userId") ? json.get("userId").asLong() : null;
            String orderCode = json.has("orderCode") ? json.get("orderCode").asText() : "";

            if (userId != null) {
                log.info("Received order-created event for user: {}, orderCode: {}", userId, orderCode);
                notificationService.createOrderNotification(userId, orderCode, "tạo thành công!");
            }
            if (ack != null) ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing order-created event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order-cancelled", groupId = "notification-service-group")
    public void handleOrderCancelled(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            JsonNode json = parseToJsonNode(record.value());
            Long userId = json.has("userId") ? json.get("userId").asLong() : null;
            String orderCode = json.has("orderCode") ? json.get("orderCode").asText() : "";
            String reason = json.has("reason") ? json.get("reason").asText() : "";

            if (userId != null) {
                log.info("Received order-cancelled event for user: {}, orderCode: {}", userId, orderCode);
                String message = "bị hủy!" + (reason.isEmpty() ? "" : " Lý do: " + reason);
                notificationService.createOrderNotification(userId, orderCode, message);
            }
            if (ack != null) ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing order-cancelled event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order-status-updated", groupId = "notification-service-group")
    public void handleOrderStatusUpdated(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            JsonNode json = parseToJsonNode(record.value());
            Long userId = json.has("userId") ? json.get("userId").asLong() : null;
            String orderCode = json.has("orderCode") ? json.get("orderCode").asText() : "";
            String newStatus = json.has("newStatus") ? json.get("newStatus").asText() : "";

            String statusText = switch (newStatus) {
                case "PENDING" -> "đang chờ xác nhận";
                case "CONFIRMED" -> "đã được xác nhận";
                case "PROCESSING" -> "đang được đóng gói và xử lý";
                case "SHIPPED" -> "đã bàn giao cho đơn vị vận chuyển";
                case "DELIVERED" -> "đã giao hàng thành công";
                case "CANCELLED" -> "đã bị hủy";
                case "RETURNED" -> "được yêu cầu hoàn trả";
                default -> "cập nhật trạng thái: " + newStatus;
            };

            if (userId != null) {
                log.info("Received order-status-updated event for user: {}, status: {}", userId, newStatus);
                notificationService.createOrderNotification(userId, orderCode, statusText);
            }
            if (ack != null) ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing order-status-updated event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "notification", groupId = "notification-service-group")
    public void handleDirectNotification(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            JsonNode json = parseToJsonNode(record.value());
            Long userId = json.has("userId") ? json.get("userId").asLong() : null;
            String title = json.has("title") ? json.get("title").asText() : "Thông báo";
            String body = json.has("body") ? json.get("body").asText() : "";
            String actionUrl = json.has("actionUrl") ? json.get("actionUrl").asText() : null;
            String imageUrl = json.has("imageUrl") ? json.get("imageUrl").asText() : null;

            if (userId != null) {
                SendNotificationRequest req = SendNotificationRequest.builder()
                        .title(title)
                        .body(body)
                        .type(NotificationType.SYSTEM)
                        .actionUrl(actionUrl)
                        .imageUrl(imageUrl)
                        .build();
                notificationService.sendNotification(userId, req);
            }
            if (ack != null) ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing notification event: {}", e.getMessage(), e);
        }
    }

    private JsonNode parseToJsonNode(Object value) throws Exception {
        if (value instanceof JsonNode node) {
            return node;
        } else if (value instanceof String str) {
            return objectMapper.readTree(str);
        } else if (value instanceof byte[] bytes) {
            return objectMapper.readTree(bytes);
        } else {
            return objectMapper.valueToTree(value);
        }
    }
}
