package com.vn.shipment.web.shipment.controller;

import com.vn.shipment.core.exception.ApiResponse;
import com.vn.shipment.web.shipment.domain.request.CancelShipmentRequest;
import com.vn.shipment.web.shipment.domain.request.CreateShipmentRequest;
import com.vn.shipment.web.shipment.domain.request.ShipmentFilterRequest;
import com.vn.shipment.web.shipment.domain.request.UpdateShipmentStatusRequest;
import com.vn.shipment.web.shipment.domain.response.ShipmentDetailResponse;
import com.vn.shipment.web.shipment.domain.response.ShipmentEventResponse;
import com.vn.shipment.web.shipment.domain.response.ShipmentResponse;
import com.vn.shipment.web.shipment.domain.response.ShipmentTrackingResponse;
import com.vn.shipment.web.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ApiResponse<ShipmentDetailResponse> createShipment(@Valid @RequestBody CreateShipmentRequest request) {
        return ApiResponse.success("Tạo đơn vận chuyển thành công", shipmentService.createShipment(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ShipmentDetailResponse> getShipmentById(@PathVariable Long id) {
        return ApiResponse.success(shipmentService.getShipmentById(id));
    }

    @GetMapping("/code/{shipmentCode}")
    public ApiResponse<ShipmentDetailResponse> getShipmentByCode(@PathVariable String shipmentCode) {
        return ApiResponse.success(shipmentService.getShipmentByCode(shipmentCode));
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<ShipmentDetailResponse> getShipmentByOrderId(@PathVariable Long orderId) {
        return ApiResponse.success(shipmentService.getShipmentByOrderId(orderId));
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ApiResponse<ShipmentDetailResponse> getShipmentByTrackingNumber(@PathVariable String trackingNumber) {
        return ApiResponse.success(shipmentService.getShipmentByTrackingNumber(trackingNumber));
    }

    @GetMapping
    public ApiResponse<List<ShipmentResponse>> getShipments(ShipmentFilterRequest filter) {
        return ApiResponse.success(shipmentService.getShipments(filter));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<ShipmentDetailResponse> updateStatus(@PathVariable Long id,
                                                           @Valid @RequestBody UpdateShipmentStatusRequest request) {
        return ApiResponse.success("Cập nhật trạng thái vận đơn thành công", shipmentService.updateStatus(id, request));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<ShipmentDetailResponse> cancelShipment(@PathVariable Long id,
                                                             @Valid @RequestBody CancelShipmentRequest request) {
        return ApiResponse.success("Hủy đơn vận chuyển thành công", shipmentService.cancelShipment(id, request));
    }

    @GetMapping("/{id}/tracking")
    public ApiResponse<ShipmentTrackingResponse> getTracking(@PathVariable Long id) {
        return ApiResponse.success(shipmentService.getTracking(id));
    }

    @GetMapping("/{id}/events")
    public ApiResponse<List<ShipmentEventResponse>> getEvents(@PathVariable Long id) {
        return ApiResponse.success(shipmentService.getEvents(id));
    }
}
