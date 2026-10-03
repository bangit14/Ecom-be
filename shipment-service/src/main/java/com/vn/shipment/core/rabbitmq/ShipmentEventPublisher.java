package com.vn.shipment.core.rabbitmq;

import com.vn.shipment.core.rabbitmq.event.ShipmentCreatedEvent;
import com.vn.shipment.core.rabbitmq.event.ShipmentStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.shipment:shobbe.shipment.exchange}")
    private String shipmentExchange;

    @Value("${app.rabbitmq.queues.status-updated.routing-key:shobbe.shipment.status.key}")
    private String statusRoutingKey;

    public void publishStatusChanged(ShipmentStatusChangedEvent event) {
        try {
            log.info("Publishing ShipmentStatusChangedEvent for shipmentCode: {}, newStatus: {}", 
                    event.getShipmentCode(), event.getNewStatus());
            rabbitTemplate.convertAndSend(shipmentExchange, statusRoutingKey, event);
        } catch (Exception e) {
            log.error("Failed to publish ShipmentStatusChangedEvent: {}", e.getMessage(), e);
        }
    }

    public void publishShipmentCreated(ShipmentCreatedEvent event) {
        try {
            log.info("Publishing ShipmentCreatedEvent for shipmentCode: {}", event.getShipmentCode());
            rabbitTemplate.convertAndSend(shipmentExchange, statusRoutingKey, event);
        } catch (Exception e) {
            log.error("Failed to publish ShipmentCreatedEvent: {}", e.getMessage(), e);
        }
    }
}
