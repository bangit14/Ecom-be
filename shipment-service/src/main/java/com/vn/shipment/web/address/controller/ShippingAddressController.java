package com.vn.shipment.web.address.controller;

import com.vn.shipment.core.exception.ApiResponse;
import com.vn.shipment.web.address.domain.response.ShippingDistrictResponse;
import com.vn.shipment.web.address.domain.response.ShippingProvinceResponse;
import com.vn.shipment.web.address.domain.response.ShippingWardResponse;
import com.vn.shipment.web.address.service.ShippingAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipping-addresses")
@RequiredArgsConstructor
public class ShippingAddressController {

    private final ShippingAddressService addressService;

    @GetMapping("/provinces/provider/{providerId}")
    public ApiResponse<List<ShippingProvinceResponse>> getProvincesByProviderId(@PathVariable Long providerId) {
        return ApiResponse.success(addressService.getProvincesByProviderId(providerId));
    }

    @GetMapping("/provinces/carrier/{providerCode}")
    public ApiResponse<List<ShippingProvinceResponse>> getProvincesByCarrierCode(@PathVariable String providerCode) {
        return ApiResponse.success(addressService.getProvincesByProviderCode(providerCode));
    }

    @GetMapping("/districts/province/{provinceRefId}")
    public ApiResponse<List<ShippingDistrictResponse>> getDistrictsByProvinceId(@PathVariable Long provinceRefId) {
        return ApiResponse.success(addressService.getDistrictsByProvinceId(provinceRefId));
    }

    @GetMapping("/wards/district/{districtRefId}")
    public ApiResponse<List<ShippingWardResponse>> getWardsByDistrictId(@PathVariable Long districtRefId) {
        return ApiResponse.success(addressService.getWardsByDistrictId(districtRefId));
    }
}
