package com.vn.notification.web.email.consumer;

import com.vn.notification.web.email.domain.event.RegistrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j(topic = "RegistrationDlqConsumer")
public class RegistrationDlqConsumer {

    @RabbitListener(queues = "${app.rabbitmq.queues.registration.dlq-name:shobbe.auth.registration.dlq}")
    public void handleDeadLetterMessage(RegistrationEvent event, Message message) {
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        Object exceptionMessage = headers.get("x-exception-message");
        Object origExchange = headers.get("x-original-exchange");
        Object origRoutingKey = headers.get("x-original-routingKey");

        log.error("[DLQ ALERT] Permanent email send failure for recipient={}, userId={} after exhausting all retry attempts. Exception: [{}]. Original exchange: {}, Original routingKey: {}",
                event.email(), event.userId(), exceptionMessage, origExchange, origRoutingKey);
    }
}
