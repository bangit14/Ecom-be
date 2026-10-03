package com.vn.shipment.web.shipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vn.shipment.core.enums.EventSource;
import com.vn.shipment.core.enums.ShipmentStatus;
import com.vn.shipment.core.enums.SyncSource;
import com.vn.shipment.core.exception.AppException;
import com.vn.shipment.core.exception.ErrorCode;
import com.vn.shipment.core.rabbitmq.ShipmentEventPublisher;
import com.vn.shipment.core.rabbitmq.event.OrderPlacedShipmentEvent;
import com.vn.shipment.core.rabbitmq.event.ShipmentCreatedEvent;
import com.vn.shipment.core.rabbitmq.event.ShipmentStatusChangedEvent;
import com.vn.shipment.core.util.ShipmentCodeGenerator;
import com.vn.shipment.web.provider.domain.entity.ShippingProvider;
import com.vn.shipment.web.provider.domain.entity.ShippingProviderConfig;
import com.vn.shipment.web.provider.domain.entity.ShippingProviderService;
import com.vn.shipment.web.provider.mapper.ShippingProviderConfigMapper;
import com.vn.shipment.web.provider.mapper.ShippingProviderMapper;
import com.vn.shipment.web.provider.mapper.ShippingProviderServiceMapper;
import com.vn.shipment.web.shipment.domain.entity.Shipment;
import com.vn.shipment.web.shipment.domain.entity.ShipmentEvent;
import com.vn.shipment.web.shipment.domain.entity.ShipmentItem;
import com.vn.shipment.web.shipment.domain.entity.ShipmentTracking;
import com.vn.shipment.web.shipment.domain.request.*;
import com.vn.shipment.web.shipment.domain.response.*;
import com.vn.shipment.web.shipment.mapper.ShipmentEventMapper;
import com.vn.shipment.web.shipment.mapper.ShipmentItemMapper;
import com.vn.shipment.web.shipment.mapper.ShipmentMapper;
import com.vn.shipment.web.shipment.mapper.ShipmentTrackingMapper;
import com.vn.shipment.web.shipment.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentMapper shipmentMapper;
    private final ShipmentItemMapper shipmentItemMapper;
    private final ShipmentTrackingMapper trackingMapper;
    private final ShipmentEventMapper eventMapper;

    private final ShippingProviderMapper providerMapper;
    private final ShippingProviderConfigMapper configMapper;
    private final ShippingProviderServiceMapper serviceMapper;

    private final ShipmentEventPublisher eventPublisher;
    @org.springframework.context.annotation.Lazy
    private final com.vn.shipment.web.ghn.service.GhnOrderService ghnOrderService;

    @Override
    @Transactional
    public ShipmentDetailResponse createShipment(CreateShipmentRequest request) {
        // 1. Resolve Provider
        ShippingProvider provider = resolveProvider(request.getProviderId(), request.getProviderCode());

        // Delegate to GHN if provider is GHN
        if ("GHN".equalsIgnoreCase(provider.getCode()) && ghnOrderService != null) {
            try {
                return ghnOrderService.createShipmentWithGhn(request);
            } catch (Exception e) {
                log.warn("Không thể gọi tạo đơn trực tiếp trên GHN ({}), chuyển sang xử lý tạo vận đơn nội bộ.", e.getMessage());
            }
        }

        // 2. Resolve Config
        ShippingProviderConfig config = resolveConfig(provider.getId(), request.getProviderConfigId());

        // 3. Resolve Service
        ShippingProviderService service = resolveService(provider.getId(), request.getServiceId(), request.getServiceCode());

        // 4. Generate Codes
        String shipmentCode = ShipmentCodeGenerator.generateShipmentCode();
        String externalCode = provider.getCode() + "-" + System.currentTimeMillis();
        String trackingNumber = ShipmentCodeGenerator.generateTrackingNumber(provider.getCode());

        // Sender fallback from config if not given
        String sName = StringUtils.hasText(request.getSenderName()) ? request.getSenderName() : config.getDefaultPickupName();
        String sPhone = StringUtils.hasText(request.getSenderPhone()) ? request.getSenderPhone() : config.getDefaultPickupPhone();
        String sAddr = StringUtils.hasText(request.getSenderAddress()) ? request.getSenderAddress() : config.getDefaultPickupAddress();
        String sWard = StringUtils.hasText(request.getSenderWard()) ? request.getSenderWard() : config.getDefaultPickupWard();
        String sDistrict = StringUtils.hasText(request.getSenderDistrict()) ? request.getSenderDistrict() : config.getDefaultPickupDistrict();
        String sProvince = StringUtils.hasText(request.getSenderProvince()) ? request.getSenderProvince() : config.getDefaultPickupProvince();

        // 5. Create Shipment Record
        Shipment shipment = Shipment.builder()
                .orderId(request.getOrderId())
                .providerId(provider.getId())
                .providerConfigId(config.getId())
                .serviceId(service != null ? service.getId() : null)
                .shipmentCode(shipmentCode)
                .externalOrderCode(externalCode)
                .status(ShipmentStatus.CONFIRMED.name())
                .senderName(sName)
                .senderPhone(sPhone)
                .senderAddress(sAddr)
                .senderWard(sWard)
                .senderDistrict(sDistrict)
                .senderProvince(sProvince)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .receiverAddress(request.getReceiverAddress())
                .receiverWard(request.getReceiverWard())
                .receiverDistrict(request.getReceiverDistrict())
                .receiverProvince(request.getReceiverProvince())
                .codAmount(request.getCodAmount() != null ? request.getCodAmount() : BigDecimal.ZERO)
                .insuranceAmount(request.getInsuranceAmount() != null ? request.getInsuranceAmount() : BigDecimal.ZERO)
                .shippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO)
                .totalWeightGram(request.getTotalWeightGram())
                .totalValue(request.getTotalValue())
                .note(request.getNote())
                .expectedDeliveryAt(LocalDateTime.now().plusDays(2))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        shipmentMapper.insert(shipment);

        // 6. Create Items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (ShipmentItemRequest itemReq : request.getItems()) {
                ShipmentItem item = ShipmentItem.builder()
                        .shipmentId(shipment.getId())
                        .orderItemId(itemReq.getOrderItemId())
                        .productName(itemReq.getProductName())
                        .sku(itemReq.getSku())
                        .quantity(itemReq.getQuantity() != null ? itemReq.getQuantity() : 1)
                        .unitWeightGram(itemReq.getUnitWeightGram())
                        .unitPrice(itemReq.getUnitPrice())
                        .category(itemReq.getCategory())
                        .build();
                shipmentItemMapper.insert(item);
                shipment.getItems().add(item);
            }
        }

        // 7. Create Tracking Record
        String labelUrl = "https://label.shobbe.vn/shipping/print/" + trackingNumber;
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + trackingNumber;

        ShipmentTracking tracking = ShipmentTracking.builder()
                .shipmentId(shipment.getId())
                .trackingNumber(trackingNumber)
                .labelUrl(labelUrl)
                .qrCodeUrl(qrUrl)
                .currentStatus("READY_TO_PICK")
                .currentStatusDescription("Đơn hàng sẵn sàng để bàn giao cho đối tác vận chuyển " + provider.getName())
                .lastSyncedAt(LocalDateTime.now())
                .syncSource(SyncSource.MANUAL.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        trackingMapper.insert(tracking);
        shipment.setTracking(tracking);

        // 8. Create Initial Event
        ShipmentEvent event = ShipmentEvent.builder()
                .shipmentId(shipment.getId())
                .trackingId(tracking.getId())
                .eventCode("CREATED")
                .eventStatus(ShipmentStatus.CONFIRMED.name())
                .eventDescription("Đơn vận chuyển đã được tạo thành công trên hệ thống Shobbe.")
                .eventTime(LocalDateTime.now())
                .source(EventSource.MANUAL.name())
                .rawPayload("{\"message\": \"Shipment initialized successfully\"}")
                .createdAt(LocalDateTime.now())
                .build();

        eventMapper.insert(event);
        shipment.setEvents(Collections.singletonList(event));

        // 9. Publish RabbitMQ Event
        eventPublisher.publishShipmentCreated(ShipmentCreatedEvent.builder()
                .shipmentId(shipment.getId())
                .orderId(shipment.getOrderId())
                .shipmentCode(shipment.getShipmentCode())
                .externalOrderCode(shipment.getExternalOrderCode())
                .status(shipment.getStatus())
                .trackingNumber(trackingNumber)
                .createdAt(shipment.getCreatedAt())
                .build());

        return toDetailResponse(shipment, provider, service);
    }

    @Override
    @Transactional
    public ShipmentDetailResponse createShipmentFromOrderEvent(OrderPlacedShipmentEvent event) {
        log.info("Creating shipment from order event for orderId: {}", event.getOrderId());

        List<ShipmentItemRequest> itemRequests = event.getItems() != null ? event.getItems().stream()
                .map(i -> ShipmentItemRequest.builder()
                        .orderItemId(i.getOrderItemId())
                        .productName(i.getProductName())
                        .sku(i.getSku())
                        .quantity(i.getQuantity())
                        .unitWeightGram(i.getUnitWeightGram())
                        .unitPrice(i.getUnitPrice())
                        .category(i.getCategory())
                        .build())
                .collect(Collectors.toList()) : Collections.emptyList();

        CreateShipmentRequest request = CreateShipmentRequest.builder()
                .orderId(event.getOrderId())
                .providerCode(event.getProviderCode() != null ? event.getProviderCode() : "GHN")
                .serviceCode(event.getServiceCode())
                .senderName(event.getSenderName())
                .senderPhone(event.getSenderPhone())
                .senderAddress(event.getSenderAddress())
                .senderWard(event.getSenderWard())
                .senderDistrict(event.getSenderDistrict())
                .senderProvince(event.getSenderProvince())
                .receiverName(event.getReceiverName())
                .receiverPhone(event.getReceiverPhone())
                .receiverAddress(event.getReceiverAddress())
                .receiverWard(event.getReceiverWard())
                .receiverDistrict(event.getReceiverDistrict())
                .receiverProvince(event.getReceiverProvince())
                .codAmount(event.getCodAmount())
                .insuranceAmount(event.getInsuranceAmount())
                .shippingFee(event.getShippingFee())
                .totalWeightGram(event.getTotalWeightGram())
                .totalValue(event.getTotalValue())
                .note(event.getNote())
                .items(itemRequests)
                .build();

        return createShipment(request);
    }

    @Override
    public ShipmentDetailResponse getShipmentById(Long id) {
        Shipment shipment = shipmentMapper.selectById(id);
        if (shipment == null) {
            throw new AppException(ErrorCode.SHIPMENT_NOT_FOUND, "Không tìm thấy đơn vận chuyển với id: " + id);
        }
        return loadAndBuildDetail(shipment);
    }

    @Override
    public ShipmentDetailResponse getShipmentByCode(String shipmentCode) {
        Shipment shipment = shipmentMapper.findByShipmentCode(shipmentCode)
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND, "Không tìm thấy đơn vận chuyển: " + shipmentCode));
        return loadAndBuildDetail(shipment);
    }

    @Override
    public ShipmentDetailResponse getShipmentByOrderId(Long orderId) {
        Shipment shipment = shipmentMapper.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND, "Không tìm thấy đơn vận chuyển cho đơn hàng: " + orderId));
        return loadAndBuildDetail(shipment);
    }

    @Override
    public ShipmentDetailResponse getShipmentByTrackingNumber(String trackingNumber) {
        ShipmentTracking tracking = trackingMapper.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new AppException(ErrorCode.TRACKING_NOT_FOUND, "Không tìm thấy thông tin cho mã vận đơn: " + trackingNumber));
        Shipment shipment = shipmentMapper.selectById(tracking.getShipmentId());
        if (shipment == null) {
            throw new AppException(ErrorCode.SHIPMENT_NOT_FOUND);
        }
        return loadAndBuildDetail(shipment);
    }

    @Override
    public List<ShipmentResponse> getShipments(ShipmentFilterRequest filter) {
        LambdaQueryWrapper<Shipment> query = new LambdaQueryWrapper<>();
        if (filter.getOrderId() != null) query.eq(Shipment::getOrderId, filter.getOrderId());
        if (filter.getProviderId() != null) query.eq(Shipment::getProviderId, filter.getProviderId());
        if (StringUtils.hasText(filter.getStatus())) query.eq(Shipment::getStatus, filter.getStatus().toUpperCase());
        if (StringUtils.hasText(filter.getShipmentCode())) query.like(Shipment::getShipmentCode, filter.getShipmentCode());
        if (StringUtils.hasText(filter.getReceiverPhone())) query.like(Shipment::getReceiverPhone, filter.getReceiverPhone());

        query.orderByDesc(Shipment::getId);

        Page<Shipment> page = new Page<>(filter.getPage(), filter.getSize());
        List<Shipment> records = shipmentMapper.selectPage(page, query).getRecords();

        return records.stream().map(this::toSummaryResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShipmentDetailResponse updateStatus(Long id, UpdateShipmentStatusRequest request) {
        Shipment shipment = shipmentMapper.selectById(id);
        if (shipment == null) {
            throw new AppException(ErrorCode.SHIPMENT_NOT_FOUND);
        }

        String oldStatus = shipment.getStatus();
        String newStatus = request.getStatus().toUpperCase();
        shipment.setStatus(newStatus);
        shipment.setUpdatedAt(LocalDateTime.now());

        if (ShipmentStatus.DELIVERED.name().equalsIgnoreCase(newStatus)) {
            shipment.setDeliveredAt(LocalDateTime.now());
        } else if (ShipmentStatus.PICKING.name().equalsIgnoreCase(newStatus) || ShipmentStatus.IN_TRANSIT.name().equalsIgnoreCase(newStatus)) {
            if (shipment.getPickedUpAt() == null) {
                shipment.setPickedUpAt(LocalDateTime.now());
            }
        }

        shipmentMapper.updateById(shipment);

        // Update tracking
        ShipmentTracking tracking = trackingMapper.findByShipmentId(shipment.getId()).orElse(null);
        if (tracking != null) {
            tracking.setCurrentStatus(newStatus);
            if (StringUtils.hasText(request.getStatusDescription())) {
                tracking.setCurrentStatusDescription(request.getStatusDescription());
            }
            tracking.setLastSyncedAt(LocalDateTime.now());
            tracking.setUpdatedAt(LocalDateTime.now());
            trackingMapper.updateById(tracking);
        }

        // Add event
        ShipmentEvent event = ShipmentEvent.builder()
                .shipmentId(shipment.getId())
                .trackingId(tracking != null ? tracking.getId() : null)
                .eventCode(newStatus)
                .eventStatus(newStatus)
                .eventDescription(StringUtils.hasText(request.getStatusDescription()) ? request.getStatusDescription() : "Trạng thái vận chuyển: " + newStatus)
                .eventTime(request.getEventTime() != null ? request.getEventTime() : LocalDateTime.now())
                .source(EventSource.MANUAL.name())
                .rawPayload("{\"note\": \"" + (request.getNote() != null ? request.getNote() : "") + "\"}")
                .createdAt(LocalDateTime.now())
                .build();
        eventMapper.insert(event);

        // Publish event
        ShippingProvider provider = providerMapper.selectById(shipment.getProviderId());
        eventPublisher.publishStatusChanged(ShipmentStatusChangedEvent.builder()
                .shipmentId(shipment.getId())
                .orderId(shipment.getOrderId())
                .shipmentCode(shipment.getShipmentCode())
                .externalOrderCode(shipment.getExternalOrderCode())
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .trackingNumber(tracking != null ? tracking.getTrackingNumber() : null)
                .carrierName(provider != null ? provider.getName() : null)
                .note(request.getNote())
                .updatedAt(LocalDateTime.now())
                .build());

        return loadAndBuildDetail(shipment);
    }

    @Override
    @Transactional
    public ShipmentDetailResponse cancelShipment(Long id, CancelShipmentRequest request) {
        Shipment shipment = shipmentMapper.selectById(id);
        if (shipment == null) {
            throw new AppException(ErrorCode.SHIPMENT_NOT_FOUND);
        }

        if (ShipmentStatus.DELIVERED.name().equalsIgnoreCase(shipment.getStatus()) ||
            ShipmentStatus.CANCELLED.name().equalsIgnoreCase(shipment.getStatus())) {
            throw new AppException(ErrorCode.SHIPMENT_CANNOT_CANCEL, "Không thể hủy đơn vận chuyển ở trạng thái " + shipment.getStatus());
        }

        String oldStatus = shipment.getStatus();
        shipment.setStatus(ShipmentStatus.CANCELLED.name());
        shipment.setCancelledAt(LocalDateTime.now());
        shipment.setCancelReason(request.getCancelReason());
        shipment.setUpdatedAt(LocalDateTime.now());

        shipmentMapper.updateById(shipment);

        // Update tracking
        ShipmentTracking tracking = trackingMapper.findByShipmentId(shipment.getId()).orElse(null);
        if (tracking != null) {
            tracking.setCurrentStatus(ShipmentStatus.CANCELLED.name());
            tracking.setCurrentStatusDescription("Đơn vận chuyển đã bị hủy: " + request.getCancelReason());
            tracking.setLastSyncedAt(LocalDateTime.now());
            tracking.setUpdatedAt(LocalDateTime.now());
            trackingMapper.updateById(tracking);
        }

        // Add event
        ShipmentEvent event = ShipmentEvent.builder()
                .shipmentId(shipment.getId())
                .trackingId(tracking != null ? tracking.getId() : null)
                .eventCode("CANCELLED")
                .eventStatus(ShipmentStatus.CANCELLED.name())
                .eventDescription("Đơn vận chuyển bị hủy với lý do: " + request.getCancelReason())
                .eventTime(LocalDateTime.now())
                .source(EventSource.MANUAL.name())
                .rawPayload("{\"cancelReason\": \"" + request.getCancelReason() + "\"}")
                .createdAt(LocalDateTime.now())
                .build();
        eventMapper.insert(event);

        // Publish event
        ShippingProvider provider = providerMapper.selectById(shipment.getProviderId());
        eventPublisher.publishStatusChanged(ShipmentStatusChangedEvent.builder()
                .shipmentId(shipment.getId())
                .orderId(shipment.getOrderId())
                .shipmentCode(shipment.getShipmentCode())
                .externalOrderCode(shipment.getExternalOrderCode())
                .oldStatus(oldStatus)
                .newStatus(ShipmentStatus.CANCELLED.name())
                .trackingNumber(tracking != null ? tracking.getTrackingNumber() : null)
                .carrierName(provider != null ? provider.getName() : null)
                .note(request.getCancelReason())
                .updatedAt(LocalDateTime.now())
                .build());

        return loadAndBuildDetail(shipment);
    }

    @Override
    public ShipmentTrackingResponse getTracking(Long shipmentId) {
        ShipmentTracking tracking = trackingMapper.findByShipmentId(shipmentId)
                .orElseThrow(() -> new AppException(ErrorCode.TRACKING_NOT_FOUND));
        Shipment shipment = shipmentMapper.selectById(shipmentId);
        ShippingProvider provider = shipment != null ? providerMapper.selectById(shipment.getProviderId()) : null;
        return toTrackingResponse(tracking, provider);
    }

    @Override
    public List<ShipmentEventResponse> getEvents(Long shipmentId) {
        List<ShipmentEvent> events = eventMapper.findByShipmentId(shipmentId);
        return events.stream().map(this::toEventResponse).collect(Collectors.toList());
    }

    private ShipmentDetailResponse loadAndBuildDetail(Shipment s) {
        ShippingProvider provider = providerMapper.selectById(s.getProviderId());
        ShippingProviderService service = s.getServiceId() != null ? serviceMapper.selectById(s.getServiceId()) : null;
        s.setItems(shipmentItemMapper.findByShipmentId(s.getId()));
        s.setTracking(trackingMapper.findByShipmentId(s.getId()).orElse(null));
        s.setEvents(eventMapper.findByShipmentId(s.getId()));
        return toDetailResponse(s, provider, service);
    }

    private ShippingProvider resolveProvider(Long providerId, String providerCode) {
        if (providerId != null) {
            ShippingProvider p = providerMapper.selectById(providerId);
            if (p != null) return p;
        }
        if (StringUtils.hasText(providerCode)) {
            Optional<ShippingProvider> pOpt = providerMapper.findByCode(providerCode.toUpperCase());
            if (pOpt.isPresent()) return pOpt.get();
        }
        return providerMapper.selectList(new LambdaQueryWrapper<ShippingProvider>().eq(ShippingProvider::getIsActive, true))
                .stream().findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PROVIDER_NOT_FOUND, "Không có nhà vận chuyển nào khả dụng"));
    }

    private ShippingProviderConfig resolveConfig(Long providerId, Long configId) {
        if (configId != null) {
            ShippingProviderConfig c = configMapper.selectById(configId);
            if (c != null) return c;
        }
        return configMapper.findDefaultConfigByProviderId(providerId)
                .orElseGet(() -> {
                    // Fallback to any active config for provider
                    return configMapper.selectList(new LambdaQueryWrapper<ShippingProviderConfig>()
                            .eq(ShippingProviderConfig::getProviderId, providerId)
                            .eq(ShippingProviderConfig::getIsActive, true))
                            .stream().findFirst()
                            .orElse(ShippingProviderConfig.builder()
                                    .providerId(providerId)
                                    .defaultPickupName("Kho Tổng Shobbe")
                                    .defaultPickupPhone("0987654321")
                                    .defaultPickupAddress("123 Nguyễn Thị Thập")
                                    .defaultPickupWard("Tân Phú")
                                    .defaultPickupDistrict("Quận 7")
                                    .defaultPickupProvince("Hồ Chí Minh")
                                    .build());
                });
    }

    private ShippingProviderService resolveService(Long providerId, Long serviceId, String serviceCode) {
        if (serviceId != null) {
            return serviceMapper.selectById(serviceId);
        }
        if (StringUtils.hasText(serviceCode)) {
            return serviceMapper.findByProviderIdAndServiceCode(providerId, serviceCode).orElse(null);
        }
        return serviceMapper.findByProviderId(providerId).stream().findFirst().orElse(null);
    }

    private ShipmentResponse toSummaryResponse(Shipment s) {
        ShippingProvider p = providerMapper.selectById(s.getProviderId());
        ShippingProviderService srv = s.getServiceId() != null ? serviceMapper.selectById(s.getServiceId()) : null;
        ShipmentTracking trk = trackingMapper.findByShipmentId(s.getId()).orElse(null);

        String trackUrl = null;
        if (p != null && p.getTrackingUrlTemplate() != null && trk != null) {
            trackUrl = p.getTrackingUrlTemplate().replace("{code}", trk.getTrackingNumber());
        }

        return ShipmentResponse.builder()
                .id(s.getId())
                .orderId(s.getOrderId())
                .providerId(s.getProviderId())
                .providerCode(p != null ? p.getCode() : null)
                .providerName(p != null ? p.getName() : null)
                .providerLogoUrl(p != null ? p.getLogoUrl() : null)
                .serviceId(s.getServiceId())
                .serviceName(srv != null ? srv.getServiceName() : null)
                .shipmentCode(s.getShipmentCode())
                .externalOrderCode(s.getExternalOrderCode())
                .status(s.getStatus())
                .receiverName(s.getReceiverName())
                .receiverPhone(s.getReceiverPhone())
                .receiverAddress(s.getReceiverAddress())
                .receiverProvince(s.getReceiverProvince())
                .codAmount(s.getCodAmount())
                .shippingFee(s.getShippingFee())
                .totalWeightGram(s.getTotalWeightGram())
                .totalValue(s.getTotalValue())
                .trackingNumber(trk != null ? trk.getTrackingNumber() : null)
                .trackingUrl(trackUrl)
                .expectedDeliveryAt(s.getExpectedDeliveryAt())
                .deliveredAt(s.getDeliveredAt())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private ShipmentDetailResponse toDetailResponse(Shipment s, ShippingProvider p, ShippingProviderService srv) {
        ShipmentTracking trk = s.getTracking() != null ? s.getTracking() : trackingMapper.findByShipmentId(s.getId()).orElse(null);
        List<ShipmentItem> items = s.getItems() != null && !s.getItems().isEmpty() ? s.getItems() : shipmentItemMapper.findByShipmentId(s.getId());
        List<ShipmentEvent> events = s.getEvents() != null && !s.getEvents().isEmpty() ? s.getEvents() : eventMapper.findByShipmentId(s.getId());

        return ShipmentDetailResponse.builder()
                .id(s.getId())
                .orderId(s.getOrderId())
                .providerId(s.getProviderId())
                .providerCode(p != null ? p.getCode() : null)
                .providerName(p != null ? p.getName() : null)
                .providerLogoUrl(p != null ? p.getLogoUrl() : null)
                .providerConfigId(s.getProviderConfigId())
                .serviceId(s.getServiceId())
                .serviceCode(srv != null ? srv.getServiceCode() : null)
                .serviceName(srv != null ? srv.getServiceName() : null)
                .shipmentCode(s.getShipmentCode())
                .externalOrderCode(s.getExternalOrderCode())
                .status(s.getStatus())
                .senderName(s.getSenderName())
                .senderPhone(s.getSenderPhone())
                .senderAddress(s.getSenderAddress())
                .senderWard(s.getSenderWard())
                .senderDistrict(s.getSenderDistrict())
                .senderProvince(s.getSenderProvince())
                .receiverName(s.getReceiverName())
                .receiverPhone(s.getReceiverPhone())
                .receiverAddress(s.getReceiverAddress())
                .receiverWard(s.getReceiverWard())
                .receiverDistrict(s.getReceiverDistrict())
                .receiverProvince(s.getReceiverProvince())
                .codAmount(s.getCodAmount())
                .insuranceAmount(s.getInsuranceAmount())
                .shippingFee(s.getShippingFee())
                .totalWeightGram(s.getTotalWeightGram())
                .totalValue(s.getTotalValue())
                .note(s.getNote())
                .expectedDeliveryAt(s.getExpectedDeliveryAt())
                .pickedUpAt(s.getPickedUpAt())
                .deliveredAt(s.getDeliveredAt())
                .cancelledAt(s.getCancelledAt())
                .cancelReason(s.getCancelReason())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .tracking(toTrackingResponse(trk, p))
                .items(items.stream().map(this::toItemResponse).collect(Collectors.toList()))
                .events(events.stream().map(this::toEventResponse).collect(Collectors.toList()))
                .build();
    }

    private ShipmentTrackingResponse toTrackingResponse(ShipmentTracking t, ShippingProvider p) {
        if (t == null) return null;
        String trackUrl = null;
        if (p != null && p.getTrackingUrlTemplate() != null) {
            trackUrl = p.getTrackingUrlTemplate().replace("{code}", t.getTrackingNumber());
        }

        return ShipmentTrackingResponse.builder()
                .id(t.getId())
                .shipmentId(t.getShipmentId())
                .trackingNumber(t.getTrackingNumber())
                .labelUrl(t.getLabelUrl())
                .qrCodeUrl(t.getQrCodeUrl())
                .currentStatus(t.getCurrentStatus())
                .currentStatusDescription(t.getCurrentStatusDescription())
                .lastSyncedAt(t.getLastSyncedAt())
                .syncSource(t.getSyncSource())
                .trackingUrl(trackUrl)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private ShipmentItemResponse toItemResponse(ShipmentItem i) {
        return ShipmentItemResponse.builder()
                .id(i.getId())
                .shipmentId(i.getShipmentId())
                .orderItemId(i.getOrderItemId())
                .productName(i.getProductName())
                .sku(i.getSku())
                .quantity(i.getQuantity())
                .unitWeightGram(i.getUnitWeightGram())
                .unitPrice(i.getUnitPrice())
                .category(i.getCategory())
                .build();
    }

    private ShipmentEventResponse toEventResponse(ShipmentEvent e) {
        return ShipmentEventResponse.builder()
                .id(e.getId())
                .shipmentId(e.getShipmentId())
                .trackingId(e.getTrackingId())
                .eventCode(e.getEventCode())
                .eventStatus(e.getEventStatus())
                .eventDescription(e.getEventDescription())
                .eventTime(e.getEventTime())
                .source(e.getSource())
                .rawPayload(e.getRawPayload())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
