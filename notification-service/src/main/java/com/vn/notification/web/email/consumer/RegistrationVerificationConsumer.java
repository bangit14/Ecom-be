package com.vn.notification.web.email.consumer;

import com.vn.notification.web.email.domain.event.RegistrationEvent;
import com.vn.notification.web.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "RegistrationVerificationConsumer")
public class RegistrationVerificationConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "${app.rabbitmq.queues.registration.name:shobbe.auth.registration.queue}")
    public void handleRegistrationVerification(RegistrationEvent event) {
        log.info("Received registration verification event from RabbitMQ: userId={}, email={}, token={}",
                event.userId(), event.email(), event.verificationToken());

        emailService.sendVerificationEmail(event.email(), event.fullName(), event.verificationToken());
    }
}
