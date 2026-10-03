package com.vn.shipment.web.provider.service;

import com.vn.shipment.web.provider.domain.request.*;
import com.vn.shipment.web.provider.domain.response.ShippingProviderConfigResponse;
import com.vn.shipment.web.provider.domain.response.ShippingProviderResponse;
import com.vn.shipment.web.provider.domain.response.ShippingProviderServiceResponse;

import java.util.List;

public interface ShippingProviderService {

    List<ShippingProviderResponse> getAllProviders();

    ShippingProviderResponse getProviderById(Long id);

    ShippingProviderResponse getProviderByCode(String code);

    ShippingProviderResponse createProvider(CreateShippingProviderRequest request);

    ShippingProviderResponse updateProvider(Long id, UpdateShippingProviderRequest request);

    List<ShippingProviderConfigResponse> getConfigsByProviderId(Long providerId);

    ShippingProviderConfigResponse getDefaultConfig(String providerCode);

    ShippingProviderConfigResponse createConfig(CreateProviderConfigRequest request);

    ShippingProviderConfigResponse updateConfig(Long configId, UpdateProviderConfigRequest request);

    List<ShippingProviderServiceResponse> getServicesByProviderId(Long providerId);

    ShippingProviderServiceResponse createService(CreateProviderServiceRequest request);

    // Seller Settings
    List<ShippingProviderConfigResponse> getSellerConfigs(Long tenantId);

    ShippingProviderConfigResponse saveSellerConfig(Long tenantId, com.vn.shipment.web.provider.domain.dto.SaveSellerShippingConfigRequest request);
}
