package com.vn.shipment.web.ghn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.shipment.core.enums.EventSource;
import com.vn.shipment.core.enums.GhnOrderStatus;
import com.vn.shipment.core.enums.GhnPaymentType;
import com.vn.shipment.core.enums.GhnRequiredNote;
import com.vn.shipment.core.enums.ShipmentStatus;
import com.vn.shipment.core.enums.SyncSource;
import com.vn.shipment.core.exception.AppException;
import com.vn.shipment.core.exception.ErrorCode;
import com.vn.shipment.core.rabbitmq.ShipmentEventPublisher;
import com.vn.shipment.core.rabbitmq.event.ShipmentCreatedEvent;
import com.vn.shipment.core.util.ShipmentCodeGenerator;
import com.vn.shipment.web.ghn.client.GhnApiClient;
import com.vn.shipment.web.ghn.domain.dto.GhnFeeDetailDto;
import com.vn.shipment.web.ghn.domain.dto.GhnOrderItemDto;
import com.vn.shipment.web.ghn.domain.request.*;
import com.vn.shipment.web.ghn.domain.response.GhnCreateOrderResponse;
import com.vn.shipment.web.ghn.domain.response.GhnPrintOrderResponse;
import com.vn.shipment.web.ghn.service.GhnOrderService;
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
import com.vn.shipment.web.shipment.domain.request.CreateShipmentRequest;
import com.vn.shipment.web.shipment.domain.response.ShipmentDetailResponse;
import com.vn.shipment.web.shipment.domain.response.ShipmentEventResponse;
import com.vn.shipment.web.shipment.domain.response.ShipmentItemResponse;
import com.vn.shipment.web.shipment.domain.response.ShipmentTrackingResponse;
import com.vn.shipment.web.shipment.mapper.ShipmentEventMapper;
import com.vn.shipment.web.shipment.mapper.ShipmentItemMapper;
import com.vn.shipment.web.shipment.mapper.ShipmentMapper;
import com.vn.shipment.web.shipment.mapper.ShipmentTrackingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GhnOrderServiceImpl implements GhnOrderService {

    private final GhnApiClient ghnApiClient;
    private final ShippingProviderMapper providerMapper;
    private final ShippingProviderConfigMapper configMapper;
    private final ShippingProviderServiceMapper serviceMapper;
    private final ShipmentMapper shipmentMapper;
    private final ShipmentItemMapper shipmentItemMapper;
    private final ShipmentTrackingMapper trackingMapper;
    private final ShipmentEventMapper eventMapper;
    private final ShipmentEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    public GhnCreateOrderResponse createOrder(GhnCreateOrderRequest request, Long shopId) {
        return ghnApiClient.createOrder(request, null, shopId);
    }

    @Override
    public GhnFeeDetailDto calculateFee(GhnCalculateFeeRequest request, Long shopId) {
        return ghnApiClient.calculateFee(request, null, shopId);
    }

    @Override
    public LocalDateTime calculateLeadtime(GhnLeadtimeRequest request, Long shopId) {
        return ghnApiClient.calculateLeadtime(request, null, shopId);
    }

    @Override
    @Transactional
    public void cancelOrder(GhnCancelOrderRequest request, Long shopId) {
        ghnApiClient.cancelOrder(request, null, shopId);

        // Update database if matching shipments exist
        if (request.getOrderCodes() != null) {
            for (String code : request.getOrderCodes()) {
                Optional<Shipment> shipOpt = shipmentMapper.findByExternalOrderCode(code);
                if (shipOpt.isPresent()) {
                    Shipment shipment = shipOpt.get();
                    shipment.setStatus(ShipmentStatus.CANCELLED.name());
                    shipment.setCancelledAt(LocalDateTime.now());
                    shipment.setCancelReason(request.getReason() != null ? request.getReason() : request.getReasonCode());
                    shipment.setUpdatedAt(LocalDateTime.now());
                    shipmentMapper.updateById(shipment);

                    // tracking
                    trackingMapper.findByShipmentId(shipment.getId()).ifPresent(trk -> {
                        trk.setCurrentStatus(ShipmentStatus.CANCELLED.name());
                        trk.setCurrentStatusDescription("Đơn vận chuyển đã hủy trên GHN");
                        trk.setLastSyncedAt(LocalDateTime.now());
                        trk.setUpdatedAt(LocalDateTime.now());
                        trackingMapper.updateById(trk);
                    });

                    // event
                    ShipmentEvent event = ShipmentEvent.builder()
                            .shipmentId(shipment.getId())
                            .eventCode("CANCEL")
                            .eventStatus(ShipmentStatus.CANCELLED.name())
                            .eventDescription("Đã hủy đơn hàng trên hệ thống GHN (" + request.getReasonCode() + ")")
                            .eventTime(LocalDateTime.now())
                            .source(EventSource.CARRIER_API.name())
                            .rawPayload("{\"reasonCode\": \"" + request.getReasonCode() + "\"}")
                            .createdAt(LocalDateTime.now())
                            .build();
                    eventMapper.insert(event);
                }
            }
        }
    }

    @Override
    public GhnPrintOrderResponse generatePrintToken(GhnPrintOrderRequest request, Long shopId) {
        return ghnApiClient.generatePrintToken(request, null, shopId);
    }

    @Override
    @Transactional
    public ShipmentDetailResponse createShipmentWithGhn(CreateShipmentRequest request) {
        // 1. Resolve GHN Provider
        ShippingProvider provider = providerMapper.findByCode("GHN")
                .orElseThrow(() -> new AppException(ErrorCode.PROVIDER_NOT_FOUND, "Không tìm thấy nhà vận chuyển GHN"));

        // 2. Resolve Provider Config
        ShippingProviderConfig config = resolveConfig(provider.getId(), request.getProviderConfigId());

        Long ghnShopId = null;
        String ghnToken = null;
        if (StringUtils.hasText(config.getProviderShopId())) {
            try {
                ghnShopId = Long.parseLong(config.getProviderShopId().trim());
            } catch (Exception ignored) {}
        }
        if (StringUtils.hasText(config.getToken())) {
            ghnToken = config.getToken();
        }

        // 3. Resolve Service
        ShippingProviderService service = resolveService(provider.getId(), request.getServiceId(), request.getServiceCode());

        // 4. Build GHN Create Order Request
        String clientOrderCode = StringUtils.hasText(request.getOrderCode()) ? request.getOrderCode() : "ORDER-" + request.getOrderId();

        // Sender information
        String sName = StringUtils.hasText(request.getSenderName()) ? request.getSenderName() : config.getDefaultPickupName();
        String sPhone = StringUtils.hasText(request.getSenderPhone()) ? request.getSenderPhone() : config.getDefaultPickupPhone();
        String sAddr = StringUtils.hasText(request.getSenderAddress()) ? request.getSenderAddress() : config.getDefaultPickupAddress();
        String sWard = StringUtils.hasText(request.getSenderWard()) ? request.getSenderWard() : config.getDefaultPickupWard();
        String sDistrict = StringUtils.hasText(request.getSenderDistrict()) ? request.getSenderDistrict() : config.getDefaultPickupDistrict();
        String sProvince = StringUtils.hasText(request.getSenderProvince()) ? request.getSenderProvince() : config.getDefaultPickupProvince();

        int totalWeight = request.getTotalWeightGram() != null && request.getTotalWeightGram() > 0 ? request.getTotalWeightGram() : 500;
        int serviceTypeId = totalWeight >= 20000 ? 5 : 2;

        List<GhnOrderItemDto> ghnItems = new ArrayList<>();
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (var itemReq : request.getItems()) {
                ghnItems.add(GhnOrderItemDto.builder()
                        .name(itemReq.getProductName())
                        .code(itemReq.getSku())
                        .quantity(itemReq.getQuantity() != null ? itemReq.getQuantity() : 1)
                        .price(itemReq.getUnitPrice() != null ? itemReq.getUnitPrice().longValue() : 0L)
                        .weight(itemReq.getUnitWeightGram() != null ? itemReq.getUnitWeightGram() : 200)
                        .length(15)
                        .width(10)
                        .height(10)
                        .build());
            }
        }

        GhnCreateOrderRequest ghnReq = GhnCreateOrderRequest.builder()
                .paymentTypeId("BUYER".equalsIgnoreCase(config.getDefaultPayer()) ? GhnPaymentType.BUYER.getId() : GhnPaymentType.SHOP.getId())
                .requiredNote(GhnRequiredNote.CHOXEMHANGKHONGTHU.getCode())
                .clientOrderCode(clientOrderCode)
                .fromName(sName)
                .fromPhone(sPhone)
                .fromAddress(sAddr)
                .fromWardName(sWard)
                .fromDistrictName(sDistrict)
                .fromProvinceName(sProvince)
                .returnName(sName)
                .returnPhone(sPhone)
                .returnAddress(sAddr)
                .returnWardName(sWard)
                .returnDistrictName(sDistrict)
                .returnProvinceName(sProvince)
                .toName(request.getReceiverName())
                .toPhone(request.getReceiverPhone())
                .toAddress(request.getReceiverAddress())
                .toWardName(request.getReceiverWard())
                .toDistrictName(request.getReceiverDistrict())
                .toProvinceName(request.getReceiverProvince())
                .isNewToAddress(false)
                .codAmount(request.getCodAmount() != null ? request.getCodAmount().longValue() : 0L)
                .insuranceValue(request.getInsuranceAmount() != null ? request.getInsuranceAmount().longValue() : 0L)
                .orderValue(request.getTotalValue() != null ? request.getTotalValue().longValue() : 0L)
                .weight(totalWeight)
                .length(20)
                .width(15)
                .height(10)
                .serviceTypeId(serviceTypeId)
                .content(request.getNote() != null ? request.getNote() : "Đơn hàng " + clientOrderCode)
                .note(request.getNote())
                .items(ghnItems)
                .build();

        // 5. Call GHN API
        GhnCreateOrderResponse ghnResp = ghnApiClient.createOrder(ghnReq, ghnToken, ghnShopId);
        String ghnOrderCode = ghnResp.getOrderCode();

        // Parse expected delivery time
        LocalDateTime expectedDeliveryAt = null;
        if (StringUtils.hasText(ghnResp.getExpectedDeliveryTime())) {
            try {
                expectedDeliveryAt = LocalDateTime.ofInstant(Instant.parse(ghnResp.getExpectedDeliveryTime()), ZoneId.systemDefault());
            } catch (Exception ignored) {}
        }

        BigDecimal shippingFee = ghnResp.getTotalFee() != null ? BigDecimal.valueOf(ghnResp.getTotalFee()) : (request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);

        // 6. Save Shipment Record
        String shipmentCode = ShipmentCodeGenerator.generateShipmentCode();
        Shipment shipment = Shipment.builder()
                .orderId(request.getOrderId())
                .providerId(provider.getId())
                .providerConfigId(config.getId())
                .serviceId(service != null ? service.getId() : null)
                .shipmentCode(shipmentCode)
                .externalOrderCode(ghnOrderCode)
                .status(ShipmentStatus.READY_TO_PICK.name())
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
                .shippingFee(shippingFee)
                .totalWeightGram(totalWeight)
                .totalValue(request.getTotalValue())
                .note(request.getNote())
                .expectedDeliveryAt(expectedDeliveryAt)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        shipmentMapper.insert(shipment);

        // 7. Save Items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (var itemReq : request.getItems()) {
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
            }
        }

        // 8. Save Tracking
        String trackingNumber = ghnOrderCode;
        String trackingUrl = provider.getTrackingUrlTemplate() != null ? provider.getTrackingUrlTemplate().replace("{code}", trackingNumber) : "https://tracking.ghn.dev/?order_code=" + trackingNumber;

        ShipmentTracking tracking = ShipmentTracking.builder()
                .shipmentId(shipment.getId())
                .trackingNumber(trackingNumber)
                .labelUrl(trackingUrl)
                .currentStatus(GhnOrderStatus.READY_TO_PICK.name())
                .currentStatusDescription(GhnOrderStatus.READY_TO_PICK.getDescription())
                .lastSyncedAt(LocalDateTime.now())
                .syncSource(SyncSource.API.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        trackingMapper.insert(tracking);

        // 9. Save Event
        ShipmentEvent event = ShipmentEvent.builder()
                .shipmentId(shipment.getId())
                .trackingId(tracking.getId())
                .eventCode(GhnOrderStatus.READY_TO_PICK.getCode())
                .eventStatus(ShipmentStatus.READY_TO_PICK.name())
                .eventDescription("Đã tạo đơn hàng thành công trên hệ thống GHN (Mã vận đơn: " + ghnOrderCode + ")")
                .eventTime(LocalDateTime.now())
                .source(EventSource.CARRIER_API.name())
                .rawPayload("{\"orderCode\":\"" + ghnOrderCode + "\",\"totalFee\":" + shippingFee + "}")
                .createdAt(LocalDateTime.now())
                .build();
        eventMapper.insert(event);

        // 10. Publish RabbitMQ Event
        eventPublisher.publishShipmentCreated(ShipmentCreatedEvent.builder()
                .shipmentId(shipment.getId())
                .orderId(shipment.getOrderId())
                .shipmentCode(shipment.getShipmentCode())
                .externalOrderCode(ghnOrderCode)
                .trackingNumber(trackingNumber)
                .status(ShipmentStatus.READY_TO_PICK.name())
                .createdAt(LocalDateTime.now())
                .build());

        return loadAndBuildDetail(shipment);
    }

    private ShippingProviderConfig resolveConfig(Long providerId, Long configId) {
        if (configId != null) {
            ShippingProviderConfig c = configMapper.selectById(configId);
            if (c != null) return c;
        }
        return configMapper.findDefaultConfigByProviderId(providerId)
                .orElseGet(() -> configMapper.selectList(new LambdaQueryWrapper<ShippingProviderConfig>()
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
                                .build()));
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

    private ShipmentDetailResponse loadAndBuildDetail(Shipment s) {
        ShippingProvider provider = providerMapper.selectById(s.getProviderId());
        ShippingProviderService service = s.getServiceId() != null ? serviceMapper.selectById(s.getServiceId()) : null;
        s.setItems(shipmentItemMapper.findByShipmentId(s.getId()));
        s.setTracking(trackingMapper.findByShipmentId(s.getId()).orElse(null));
        s.setEvents(eventMapper.findByShipmentId(s.getId()));

        List<ShipmentItemResponse> itemResponses = s.getItems() != null ? s.getItems().stream().map(i -> ShipmentItemResponse.builder()
                .id(i.getId())
                .orderItemId(i.getOrderItemId())
                .productName(i.getProductName())
                .sku(i.getSku())
                .quantity(i.getQuantity())
                .unitWeightGram(i.getUnitWeightGram())
                .unitPrice(i.getUnitPrice())
                .category(i.getCategory())
                .build()).collect(Collectors.toList()) : Collections.emptyList();

        ShipmentTrackingResponse trackingResp = s.getTracking() != null ? ShipmentTrackingResponse.builder()
                .id(s.getTracking().getId())
                .shipmentId(s.getId())
                .trackingNumber(s.getTracking().getTrackingNumber())
                .labelUrl(s.getTracking().getLabelUrl())
                .currentStatus(s.getTracking().getCurrentStatus())
                .currentStatusDescription(s.getTracking().getCurrentStatusDescription())
                .lastSyncedAt(s.getTracking().getLastSyncedAt())
                .syncSource(s.getTracking().getSyncSource())
                .trackingUrl(s.getTracking().getLabelUrl())
                .createdAt(s.getTracking().getCreatedAt())
                .updatedAt(s.getTracking().getUpdatedAt())
                .build() : null;

        List<ShipmentEventResponse> eventResponses = s.getEvents() != null ? s.getEvents().stream().map(e -> ShipmentEventResponse.builder()
                .id(e.getId())
                .shipmentId(e.getShipmentId())
                .eventCode(e.getEventCode())
                .eventStatus(e.getEventStatus())
                .eventDescription(e.getEventDescription())
                .eventTime(e.getEventTime())
                .source(e.getSource())
                .build()).collect(Collectors.toList()) : Collections.emptyList();

        return ShipmentDetailResponse.builder()
                .id(s.getId())
                .orderId(s.getOrderId())
                .providerId(s.getProviderId())
                .providerCode(provider != null ? provider.getCode() : null)
                .providerName(provider != null ? provider.getName() : null)
                .serviceId(s.getServiceId())
                .serviceCode(service != null ? service.getServiceCode() : null)
                .serviceName(service != null ? service.getServiceName() : null)
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
                .tracking(trackingResp)
                .items(itemResponses)
                .events(eventResponses)
                .build();
    }
}
