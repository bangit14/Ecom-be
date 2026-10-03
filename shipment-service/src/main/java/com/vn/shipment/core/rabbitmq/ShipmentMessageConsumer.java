package com.vn.shipment.core.rabbitmq;

import com.vn.shipment.core.rabbitmq.event.OrderPlacedShipmentEvent;
import com.vn.shipment.web.shipment.domain.response.ShipmentDetailResponse;
import com.vn.shipment.web.shipment.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentMessageConsumer {

    private final ShipmentService shipmentService;

    @RabbitListener(queues = "${app.rabbitmq.queues.order-shipment.name:shobbe.order.shipment.queue}",
                    containerFactory = "rabbitListenerContainerFactory")
    public void handleOrderShipmentMessage(OrderPlacedShipmentEvent event) {
        log.info("Received OrderPlacedShipmentEvent from RabbitMQ for orderId: {}, orderCode: {}",
                event.getOrderId(), event.getOrderCode());

        try {
            ShipmentDetailResponse shipment = shipmentService.createShipmentFromOrderEvent(event);
            log.info("Successfully created shipment {} for order {}", shipment.getShipmentCode(), event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to process order shipment message for orderId {}: {}", event.getOrderId(), e.getMessage(), e);
            throw e; // Rethrow to trigger RabbitMQ retry & DLQ
        }
    }
}
