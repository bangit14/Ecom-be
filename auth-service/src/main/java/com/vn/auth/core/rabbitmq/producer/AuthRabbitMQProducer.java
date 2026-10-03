package com.vn.auth.core.rabbitmq.producer;

import com.vn.auth.core.rabbitmq.event.RegistrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthRabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.auth:shobbe.auth.exchange}")
    private String authExchange;

    @Value("${app.rabbitmq.queues.registration.routing-key:shobbe.auth.registration.key}")
    private String registrationRoutingKey;

    public void sendRegistrationVerification(RegistrationEvent event) {
        try {
            log.info("Publishing registration verification event to RabbitMQ for userId: {}, email: {}",
                    event.userId(), event.email());
            rabbitTemplate.convertAndSend(authExchange, registrationRoutingKey, event);
            log.info("Successfully published registration verification event to RabbitMQ for: {}", event.email());
        } catch (Exception e) {
            log.error("Failed to publish registration verification event to RabbitMQ: {}", e.getMessage(), e);
        }
    }
}
