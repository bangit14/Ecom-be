package com.vn.shipment.web.webhook.service.impl;

import com.vn.shipment.core.enums.EventSource;
import com.vn.shipment.core.enums.ShipmentStatus;
import com.vn.shipment.core.enums.SyncSource;
import com.vn.shipment.core.exception.AppException;
import com.vn.shipment.core.exception.ErrorCode;
import com.vn.shipment.core.rabbitmq.ShipmentEventPublisher;
import com.vn.shipment.core.rabbitmq.event.ShipmentStatusChangedEvent;
import com.vn.shipment.web.provider.domain.entity.ShippingProvider;
import com.vn.shipment.web.provider.mapper.ShippingProviderMapper;
import com.vn.shipment.web.shipment.domain.entity.Shipment;
import com.vn.shipment.web.shipment.domain.entity.ShipmentEvent;
import com.vn.shipment.web.shipment.domain.entity.ShipmentTracking;
import com.vn.shipment.web.shipment.domain.response.ShipmentDetailResponse;
import com.vn.shipment.web.shipment.mapper.ShipmentEventMapper;
import com.vn.shipment.web.shipment.mapper.ShipmentMapper;
import com.vn.shipment.web.shipment.mapper.ShipmentTrackingMapper;
import com.vn.shipment.web.shipment.service.ShipmentService;
import com.vn.shipment.web.webhook.domain.request.CarrierWebhookPayload;
import com.vn.shipment.web.webhook.service.ShipmentWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentWebhookServiceImpl implements ShipmentWebhookService {

    private final ShipmentMapper shipmentMapper;
    private final ShipmentTrackingMapper trackingMapper;
    private final ShipmentEventMapper eventMapper;
    private final ShippingProviderMapper providerMapper;
    private final ShipmentService shipmentService;
    private final ShipmentEventPublisher eventPublisher;

    @Override
    @Transactional
    public ShipmentDetailResponse handleCarrierWebhook(String providerCode, String payloadJson, CarrierWebhookPayload payload) {
        log.info("Received webhook from carrier [{}]: {}", providerCode, payloadJson);

        // 1. Identify tracking number or shipment code
        String trackingNumber = extractTrackingNumber(payload);
        String externalCode = extractExternalOrderCode(payload);
        String shipmentCode = payload.getShipmentCode();

        Shipment shipment = null;
        ShipmentTracking tracking = null;

        if (StringUtils.hasText(trackingNumber)) {
            Optional<ShipmentTracking> trkOpt = trackingMapper.findByTrackingNumber(trackingNumber);
            if (trkOpt.isPresent()) {
                tracking = trkOpt.get();
                shipment = shipmentMapper.selectById(tracking.getShipmentId());
            }
        }

        if (shipment == null && StringUtils.hasText(externalCode)) {
            Optional<Shipment> shipOpt = shipmentMapper.findByExternalOrderCode(externalCode);
            if (shipOpt.isPresent()) {
                shipment = shipOpt.get();
                tracking = trackingMapper.findByShipmentId(shipment.getId()).orElse(null);
            }
        }

        if (shipment == null && StringUtils.hasText(shipmentCode)) {
            Optional<Shipment> shipOpt = shipmentMapper.findByShipmentCode(shipmentCode);
            if (shipOpt.isPresent()) {
                shipment = shipOpt.get();
                tracking = trackingMapper.findByShipmentId(shipment.getId()).orElse(null);
            }
        }

        if (shipment == null) {
            log.warn("Shipment not found for webhook: trackingNumber={}, externalCode={}, shipmentCode={}",
                    trackingNumber, externalCode, shipmentCode);
            throw new AppException(ErrorCode.SHIPMENT_NOT_FOUND, "Không tìm thấy vận đơn phù hợp với webhook");
        }

        // 2. Normalize status
        String rawStatus = extractRawStatus(payload);
        ShipmentStatus normalizedStatus = mapToInternalStatus(providerCode, rawStatus, payload);
        String oldStatus = shipment.getStatus();

        // 3. Update shipment
        shipment.setStatus(normalizedStatus.name());
        if (normalizedStatus == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(LocalDateTime.now());
        } else if (normalizedStatus == ShipmentStatus.PICKING || normalizedStatus == ShipmentStatus.IN_TRANSIT) {
            if (shipment.getPickedUpAt() == null) {
                shipment.setPickedUpAt(LocalDateTime.now());
            }
        } else if (normalizedStatus == ShipmentStatus.CANCELLED) {
            shipment.setCancelledAt(LocalDateTime.now());
            shipment.setCancelReason(payload.getReason() != null ? payload.getReason() : "Hủy từ phía nhà vận chuyển");
        }
        shipment.setUpdatedAt(LocalDateTime.now());
        shipmentMapper.updateById(shipment);

        // 4. Update tracking
        String desc = extractDescription(payload);
        if (tracking != null) {
            tracking.setCurrentStatus(rawStatus);
            tracking.setCurrentStatusDescription(desc);
            tracking.setLastSyncedAt(LocalDateTime.now());
            tracking.setSyncSource(SyncSource.WEBHOOK.name());
            tracking.setUpdatedAt(LocalDateTime.now());
            trackingMapper.updateById(tracking);
        }

        // 5. Insert shipment event
        ShipmentEvent event = ShipmentEvent.builder()
                .shipmentId(shipment.getId())
                .trackingId(tracking != null ? tracking.getId() : null)
                .eventCode(rawStatus != null ? rawStatus : normalizedStatus.name())
                .eventStatus(normalizedStatus.name())
                .eventDescription(desc)
                .eventTime(payload.getEventTime() != null ? payload.getEventTime() : LocalDateTime.now())
                .source(EventSource.WEBHOOK.name())
                .rawPayload(payloadJson)
                .createdAt(LocalDateTime.now())
                .build();
        eventMapper.insert(event);

        // 6. Publish RabbitMQ status event
        ShippingProvider provider = providerMapper.selectById(shipment.getProviderId());
        eventPublisher.publishStatusChanged(ShipmentStatusChangedEvent.builder()
                .shipmentId(shipment.getId())
                .orderId(shipment.getOrderId())
                .shipmentCode(shipment.getShipmentCode())
                .externalOrderCode(shipment.getExternalOrderCode())
                .oldStatus(oldStatus)
                .newStatus(normalizedStatus.name())
                .trackingNumber(tracking != null ? tracking.getTrackingNumber() : trackingNumber)
                .carrierName(provider != null ? provider.getName() : providerCode)
                .note(desc)
                .updatedAt(LocalDateTime.now())
                .build());

        return shipmentService.getShipmentById(shipment.getId());
    }

    private String extractTrackingNumber(CarrierWebhookPayload payload) {
        if (StringUtils.hasText(payload.getTrackingNumber())) return payload.getTrackingNumber();
        if (StringUtils.hasText(payload.getGhnOrderCode())) return payload.getGhnOrderCode();
        if (StringUtils.hasText(payload.getGhtkLabelId())) return payload.getGhtkLabelId();
        return null;
    }

    private String extractExternalOrderCode(CarrierWebhookPayload payload) {
        if (StringUtils.hasText(payload.getExternalOrderCode())) return payload.getExternalOrderCode();
        if (StringUtils.hasText(payload.getGhnOrderCode())) return payload.getGhnOrderCode();
        if (StringUtils.hasText(payload.getGhtkPartnerId())) return payload.getGhtkPartnerId();
        return null;
    }

    private String extractRawStatus(CarrierWebhookPayload payload) {
        if (StringUtils.hasText(payload.getStatus())) return payload.getStatus();
        if (StringUtils.hasText(payload.getGhnStatus())) return payload.getGhnStatus();
        if (payload.getGhtkStatusId() != null) return "GHTK_STATUS_" + payload.getGhtkStatusId();
        return "UNKNOWN";
    }

    private String extractDescription(CarrierWebhookPayload payload) {
        if (StringUtils.hasText(payload.getStatusDescription())) return payload.getStatusDescription();
        if (StringUtils.hasText(payload.getGhnDescription())) return payload.getGhnDescription();
        if (StringUtils.hasText(payload.getReason())) return payload.getReason();
        return "Cập nhật trạng thái từ Webhook " + payload.getProviderCode();
    }

    private ShipmentStatus mapToInternalStatus(String providerCode, String rawStatus, CarrierWebhookPayload payload) {
        if (rawStatus == null) return ShipmentStatus.IN_TRANSIT;
        String lower = rawStatus.toLowerCase();

        // GHN mapping
        if ("ghn".equalsIgnoreCase(providerCode) || StringUtils.hasText(payload.getGhnStatus())) {
            com.vn.shipment.core.enums.GhnOrderStatus ghnStatus = com.vn.shipment.core.enums.GhnOrderStatus.fromCode(rawStatus);
            if (ghnStatus != null) {
                return ghnStatus.toShipmentStatus();
            }
            if (lower.contains("ready_to_pick")) return ShipmentStatus.READY_TO_PICK;
            if (lower.contains("picking") || lower.contains("storing")) return ShipmentStatus.PICKING;
            if (lower.contains("delivering") || lower.contains("transporting") || lower.contains("money_collect_delivering")) return ShipmentStatus.IN_TRANSIT;
            if (lower.contains("delivered")) return ShipmentStatus.DELIVERED;
            if (lower.contains("delivery_fail")) return ShipmentStatus.DELIVERY_FAILED;
            if (lower.contains("return")) return ShipmentStatus.RETURNED;
            if (lower.contains("cancel")) return ShipmentStatus.CANCELLED;
        }

        // GHTK mapping
        if ("ghtk".equalsIgnoreCase(providerCode) || payload.getGhtkStatusId() != null) {
            Integer statusId = payload.getGhtkStatusId();
            if (statusId != null) {
                switch (statusId) {
                    case 1:
                    case 2:
                        return ShipmentStatus.CONFIRMED;
                    case 3:
                        return ShipmentStatus.PICKING;
                    case 4:
                    case 12:
                    case 13:
                        return ShipmentStatus.IN_TRANSIT;
                    case 5:
                    case 6:
                        return ShipmentStatus.DELIVERED;
                    case 7:
                    case 9:
                    case 10:
                    case 11:
                        return ShipmentStatus.DELIVERY_FAILED;
                    case 20:
                    case 21:
                        return ShipmentStatus.RETURNED;
                    case -1:
                    case 0:
                        return ShipmentStatus.CANCELLED;
                }
            }
        }

        // General keyword matching
        if (lower.contains("deliver") && lower.contains("fail")) return ShipmentStatus.DELIVERY_FAILED;
        if (lower.contains("deliver")) return ShipmentStatus.DELIVERED;
        if (lower.contains("transit") || lower.contains("shipped") || lower.contains("moving")) return ShipmentStatus.IN_TRANSIT;
        if (lower.contains("pick")) return ShipmentStatus.PICKING;
        if (lower.contains("return")) return ShipmentStatus.RETURNED;
        if (lower.contains("cancel")) return ShipmentStatus.CANCELLED;

        return ShipmentStatus.IN_TRANSIT;
    }
}
