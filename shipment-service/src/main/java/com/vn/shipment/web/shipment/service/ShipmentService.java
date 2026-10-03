package com.vn.shipment.web.shipment.service;

import com.vn.shipment.core.rabbitmq.event.OrderPlacedShipmentEvent;
import com.vn.shipment.web.shipment.domain.request.CancelShipmentRequest;
import com.vn.shipment.web.shipment.domain.request.CreateShipmentRequest;
import com.vn.shipment.web.shipment.domain.request.ShipmentFilterRequest;
import com.vn.shipment.web.shipment.domain.request.UpdateShipmentStatusRequest;
import com.vn.shipment.web.shipment.domain.response.ShipmentDetailResponse;
import com.vn.shipment.web.shipment.domain.response.ShipmentEventResponse;
import com.vn.shipment.web.shipment.domain.response.ShipmentResponse;
import com.vn.shipment.web.shipment.domain.response.ShipmentTrackingResponse;

import java.util.List;

public interface ShipmentService {

    ShipmentDetailResponse createShipment(CreateShipmentRequest request);

    ShipmentDetailResponse createShipmentFromOrderEvent(OrderPlacedShipmentEvent event);

    ShipmentDetailResponse getShipmentById(Long id);

    ShipmentDetailResponse getShipmentByCode(String shipmentCode);

    ShipmentDetailResponse getShipmentByOrderId(Long orderId);

    ShipmentDetailResponse getShipmentByTrackingNumber(String trackingNumber);

    List<ShipmentResponse> getShipments(ShipmentFilterRequest filter);

    ShipmentDetailResponse updateStatus(Long id, UpdateShipmentStatusRequest request);

    ShipmentDetailResponse cancelShipment(Long id, CancelShipmentRequest request);

    ShipmentTrackingResponse getTracking(Long shipmentId);

    List<ShipmentEventResponse> getEvents(Long shipmentId);
}
