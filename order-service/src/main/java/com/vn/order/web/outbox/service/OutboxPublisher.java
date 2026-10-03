package com.vn.order.web.outbox.service;

import com.vn.order.core.enums.OutboxEventStatus;
import com.vn.order.core.util.JsonUtil;
import com.vn.order.web.outbox.domain.entity.OutboxEvent;
import com.vn.order.web.outbox.mapper.OutboxEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "OutboxPublisher")
public class OutboxPublisher {

    private final OutboxEventMapper outboxEventMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    public void publishEvents() {
        List<OutboxEvent> events;
        try {
            events = outboxEventMapper.findPendingEvents(50);
        } catch (Exception e) {
            log.warn("Could not query pending outbox events: {}", e.getMessage());
            return;
        }

        if (events.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending outbox events", events.size());
        for (OutboxEvent event : events) {
            processEvent(event);
        }
    }

    private void processEvent(OutboxEvent event) {
        try {
            Class<?> payloadClass = Class.forName(event.getPayloadClass());
            Object payload = JsonUtil.fromJson(event.getPayload(), payloadClass);

            kafkaTemplate.send(event.getTopic(), event.getKafkaKey(), payload)
                    .get(5, TimeUnit.SECONDS);

            event.setStatus(OutboxEventStatus.SENT);
            event.setProcessedAt(LocalDateTime.now());
            outboxEventMapper.updateById(event);

            log.info("Published outbox event {} to topic {}", event.getId(), event.getTopic());
        } catch (Exception e) {
            log.warn("Failed to publish outbox event {} (topic {}): {}", event.getId(), event.getTopic(), e.getMessage());
            event.setRetryCount(event.getRetryCount() + 1);
            if (event.getRetryCount() >= event.getMaxRetries()) {
                event.setStatus(OutboxEventStatus.FAILED);
                log.error("Outbox event {} reached max retries, marked FAILED", event.getId());
            }
            outboxEventMapper.updateById(event);
        }
    }
}
