package com.vn.shipment.web.ghn.controller;

import com.vn.shipment.core.exception.ApiResponse;
import com.vn.shipment.web.ghn.domain.dto.GhnFeeDetailDto;
import com.vn.shipment.web.ghn.domain.request.*;
import com.vn.shipment.web.ghn.domain.response.GhnCreateOrderResponse;
import com.vn.shipment.web.ghn.domain.response.GhnPrintOrderResponse;
import com.vn.shipment.web.ghn.service.GhnOrderService;
import com.vn.shipment.web.shipment.domain.request.CreateShipmentRequest;
import com.vn.shipment.web.shipment.domain.response.ShipmentDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/shipment/ghn/orders")
@RequiredArgsConstructor
public class GhnOrderController {

    private final GhnOrderService ghnOrderService;

    @PostMapping("/create")
    public ApiResponse<GhnCreateOrderResponse> createOrder(
            @Valid @RequestBody GhnCreateOrderRequest request,
            @RequestParam(value = "shop_id", required = false) Long shopId) {
        return ApiResponse.success("Tạo đơn hàng GHN thành công", ghnOrderService.createOrder(request, shopId));
    }

    @PostMapping("/fee")
    public ApiResponse<GhnFeeDetailDto> calculateFee(
            @Valid @RequestBody GhnCalculateFeeRequest request,
            @RequestParam(value = "shop_id", required = false) Long shopId) {
        return ApiResponse.success("Tính cước phí GHN thành công", ghnOrderService.calculateFee(request, shopId));
    }

    @PostMapping("/leadtime")
    public ApiResponse<LocalDateTime> calculateLeadtime(
            @Valid @RequestBody GhnLeadtimeRequest request,
            @RequestParam(value = "shop_id", required = false) Long shopId) {
        return ApiResponse.success("Tính thời gian giao hàng dự kiến thành công", ghnOrderService.calculateLeadtime(request, shopId));
    }

    @PostMapping("/cancel")
    public ApiResponse<Void> cancelOrder(
            @Valid @RequestBody GhnCancelOrderRequest request,
            @RequestParam(value = "shop_id", required = false) Long shopId) {
        ghnOrderService.cancelOrder(request, shopId);
        return ApiResponse.success("Hủy đơn hàng GHN thành công", null);
    }

    @PostMapping("/print")
    public ApiResponse<GhnPrintOrderResponse> generatePrintToken(
            @Valid @RequestBody GhnPrintOrderRequest request,
            @RequestParam(value = "shop_id", required = false) Long shopId) {
        return ApiResponse.success("Tạo token in vận đơn thành công", ghnOrderService.generatePrintToken(request, shopId));
    }

    @PostMapping("/shipment")
    public ApiResponse<ShipmentDetailResponse> createShipmentWithGhn(
            @Valid @RequestBody CreateShipmentRequest request) {
        return ApiResponse.success("Tạo vận đơn GHN cho đơn hàng thành công", ghnOrderService.createShipmentWithGhn(request));
    }
}
