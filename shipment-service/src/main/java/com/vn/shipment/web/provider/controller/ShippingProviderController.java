package com.vn.shipment.web.provider.controller;

import com.vn.shipment.core.exception.ApiResponse;
import com.vn.shipment.web.provider.domain.request.*;
import com.vn.shipment.web.provider.domain.response.ShippingProviderConfigResponse;
import com.vn.shipment.web.provider.domain.response.ShippingProviderResponse;
import com.vn.shipment.web.provider.domain.response.ShippingProviderServiceResponse;
import com.vn.shipment.web.provider.service.ShippingProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipping-providers")
@RequiredArgsConstructor
public class ShippingProviderController {

    private final ShippingProviderService providerService;

    @GetMapping
    public ApiResponse<List<ShippingProviderResponse>> getAllProviders() {
        return ApiResponse.success(providerService.getAllProviders());
    }

    @GetMapping("/{id}")
    public ApiResponse<ShippingProviderResponse> getProviderById(@PathVariable Long id) {
        return ApiResponse.success(providerService.getProviderById(id));
    }

    @GetMapping("/code/{code}")
    public ApiResponse<ShippingProviderResponse> getProviderByCode(@PathVariable String code) {
        return ApiResponse.success(providerService.getProviderByCode(code));
    }

    @PostMapping
    public ApiResponse<ShippingProviderResponse> createProvider(@Valid @RequestBody CreateShippingProviderRequest request) {
        return ApiResponse.success("Thêm nhà vận chuyển thành công", providerService.createProvider(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ShippingProviderResponse> updateProvider(@PathVariable Long id, @RequestBody UpdateShippingProviderRequest request) {
        return ApiResponse.success("Cập nhật nhà vận chuyển thành công", providerService.updateProvider(id, request));
    }

    @GetMapping("/{providerId}/configs")
    public ApiResponse<List<ShippingProviderConfigResponse>> getConfigsByProviderId(@PathVariable Long providerId) {
        return ApiResponse.success(providerService.getConfigsByProviderId(providerId));
    }

    @GetMapping("/code/{providerCode}/default-config")
    public ApiResponse<ShippingProviderConfigResponse> getDefaultConfig(@PathVariable String providerCode) {
        return ApiResponse.success(providerService.getDefaultConfig(providerCode));
    }

    @PostMapping("/configs")
    public ApiResponse<ShippingProviderConfigResponse> createConfig(@Valid @RequestBody CreateProviderConfigRequest request) {
        return ApiResponse.success("Thêm cấu hình đối tác thành công", providerService.createConfig(request));
    }

    @PutMapping("/configs/{configId}")
    public ApiResponse<ShippingProviderConfigResponse> updateConfig(@PathVariable Long configId, @RequestBody UpdateProviderConfigRequest request) {
        return ApiResponse.success("Cập nhật cấu hình thành công", providerService.updateConfig(configId, request));
    }

    @GetMapping("/{providerId}/services")
    public ApiResponse<List<ShippingProviderServiceResponse>> getServicesByProviderId(@PathVariable Long providerId) {
        return ApiResponse.success(providerService.getServicesByProviderId(providerId));
    }

    @PostMapping("/services")
    public ApiResponse<ShippingProviderServiceResponse> createService(@Valid @RequestBody CreateProviderServiceRequest request) {
        return ApiResponse.success("Thêm gói dịch vụ thành công", providerService.createService(request));
    }

    // --- Seller Shipping Settings Endpoints ---

    @GetMapping("/configs/seller")
    public ApiResponse<List<ShippingProviderConfigResponse>> getSellerConfigs(
            @RequestParam(required = false) Long tenantId) {
        Long resolvedTenantId = tenantId != null ? tenantId : com.vn.shipment.core.security.UserContext.getUserId();
        return ApiResponse.success(providerService.getSellerConfigs(resolvedTenantId));
    }

    @PostMapping("/configs/seller")
    public ApiResponse<ShippingProviderConfigResponse> saveSellerConfig(
            @RequestParam(required = false) Long tenantId,
            @Valid @RequestBody com.vn.shipment.web.provider.domain.dto.SaveSellerShippingConfigRequest request) {
        Long resolvedTenantId = tenantId != null ? tenantId : com.vn.shipment.core.security.UserContext.getUserId();
        return ApiResponse.success("Lưu cài đặt vận chuyển gian hàng thành công",
                providerService.saveSellerConfig(resolvedTenantId, request));
    }
}
