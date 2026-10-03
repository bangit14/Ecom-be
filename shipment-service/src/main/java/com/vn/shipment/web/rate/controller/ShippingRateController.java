package com.vn.shipment.web.rate.controller;

import com.vn.shipment.core.exception.ApiResponse;
import com.vn.shipment.web.rate.domain.request.CalculateShippingFeeRequest;
import com.vn.shipment.web.rate.domain.request.CreateShippingRateRequest;
import com.vn.shipment.web.rate.domain.response.ShippingFeeEstimateResponse;
import com.vn.shipment.web.rate.domain.response.ShippingRateResponse;
import com.vn.shipment.web.rate.service.ShippingRateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipping-rates")
@RequiredArgsConstructor
public class ShippingRateController {

    private final ShippingRateService rateService;

    @PostMapping("/calculate")
    public ApiResponse<List<ShippingFeeEstimateResponse>> calculateFees(@Valid @RequestBody CalculateShippingFeeRequest request) {
        return ApiResponse.success(rateService.calculateShippingFees(request));
    }

    @PostMapping("/calculate/{providerCode}")
    public ApiResponse<ShippingFeeEstimateResponse> calculateFeeForProvider(@PathVariable String providerCode,
                                                                           @Valid @RequestBody CalculateShippingFeeRequest request) {
        return ApiResponse.success(rateService.calculateFeeForProvider(providerCode, request));
    }

    @GetMapping
    public ApiResponse<List<ShippingRateResponse>> getAllRates() {
        return ApiResponse.success(rateService.getAllRates());
    }

    @PostMapping
    public ApiResponse<ShippingRateResponse> createRate(@Valid @RequestBody CreateShippingRateRequest request) {
        return ApiResponse.success("Tạo bảng giá vận chuyển thành công", rateService.createRate(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRate(@PathVariable Long id) {
        rateService.deleteRate(id);
        return ApiResponse.success("Xóa bảng giá thành công", null);
    }
}
