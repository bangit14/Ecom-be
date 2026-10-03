package com.vn.shipment.web.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.shipment.core.exception.AppException;
import com.vn.shipment.core.exception.ErrorCode;
import com.vn.shipment.web.provider.domain.dto.SaveSellerShippingConfigRequest;
import com.vn.shipment.web.provider.domain.entity.ShippingProvider;
import com.vn.shipment.web.provider.domain.entity.ShippingProviderConfig;
import com.vn.shipment.web.provider.domain.request.*;
import com.vn.shipment.web.provider.domain.response.ShippingProviderConfigResponse;
import com.vn.shipment.web.provider.domain.response.ShippingProviderResponse;
import com.vn.shipment.web.provider.domain.response.ShippingProviderServiceResponse;
import com.vn.shipment.web.provider.mapper.ShippingProviderConfigMapper;
import com.vn.shipment.web.provider.mapper.ShippingProviderMapper;
import com.vn.shipment.web.provider.mapper.ShippingProviderServiceMapper;
import com.vn.shipment.web.provider.service.ShippingProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingProviderServiceImpl implements ShippingProviderService {

    private final ShippingProviderMapper providerMapper;
    private final ShippingProviderConfigMapper configMapper;
    private final ShippingProviderServiceMapper serviceMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<ShippingProviderResponse> getAllProviders() {
        List<ShippingProvider> providers = providerMapper.selectList(
                new LambdaQueryWrapper<ShippingProvider>().orderByAsc(ShippingProvider::getId)
        );
        return providers.stream().map(this::toProviderResponse).collect(Collectors.toList());
    }

    @Override
    public ShippingProviderResponse getProviderById(Long id) {
        ShippingProvider provider = providerMapper.selectById(id);
        if (provider == null) {
            throw new AppException(ErrorCode.PROVIDER_NOT_FOUND, "Không tìm thấy nhà vận chuyển với id: " + id);
        }
        return toProviderResponse(provider);
    }

    @Override
    public ShippingProviderResponse getProviderByCode(String code) {
        ShippingProvider provider = providerMapper.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.PROVIDER_NOT_FOUND, "Không tìm thấy nhà vận chuyển với mã: " + code));
        return toProviderResponse(provider);
    }

    @Override
    @Transactional
    public ShippingProviderResponse createProvider(CreateShippingProviderRequest request) {
        if (providerMapper.findByCode(request.getCode()).isPresent()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Mã nhà vận chuyển đã tồn tại: " + request.getCode());
        }

        ShippingProvider provider = ShippingProvider.builder()
                .code(request.getCode().toUpperCase())
                .name(request.getName())
                .logoUrl(request.getLogoUrl())
                .supportsCod(request.getSupportsCod() != null ? request.getSupportsCod() : true)
                .supportsInsurance(request.getSupportsInsurance() != null ? request.getSupportsInsurance() : false)
                .supportsWebhook(request.getSupportsWebhook() != null ? request.getSupportsWebhook() : true)
                .trackingUrlTemplate(request.getTrackingUrlTemplate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        providerMapper.insert(provider);
        return toProviderResponse(provider);
    }

    @Override
    @Transactional
    public ShippingProviderResponse updateProvider(Long id, UpdateShippingProviderRequest request) {
        ShippingProvider provider = providerMapper.selectById(id);
        if (provider == null) {
            throw new AppException(ErrorCode.PROVIDER_NOT_FOUND);
        }

        if (request.getName() != null) provider.setName(request.getName());
        if (request.getLogoUrl() != null) provider.setLogoUrl(request.getLogoUrl());
        if (request.getSupportsCod() != null) provider.setSupportsCod(request.getSupportsCod());
        if (request.getSupportsInsurance() != null) provider.setSupportsInsurance(request.getSupportsInsurance());
        if (request.getSupportsWebhook() != null) provider.setSupportsWebhook(request.getSupportsWebhook());
        if (request.getTrackingUrlTemplate() != null) provider.setTrackingUrlTemplate(request.getTrackingUrlTemplate());
        if (request.getIsActive() != null) provider.setIsActive(request.getIsActive());
        provider.setUpdatedAt(LocalDateTime.now());

        providerMapper.updateById(provider);
        return toProviderResponse(provider);
    }

    @Override
    public List<ShippingProviderConfigResponse> getConfigsByProviderId(Long providerId) {
        List<ShippingProviderConfig> configs = configMapper.selectList(
                new LambdaQueryWrapper<ShippingProviderConfig>()
                        .eq(ShippingProviderConfig::getProviderId, providerId)
                        .orderByDesc(ShippingProviderConfig::getIsDefault)
        );
        ShippingProvider provider = providerMapper.selectById(providerId);
        return configs.stream().map(c -> toConfigResponse(c, provider)).collect(Collectors.toList());
    }

    @Override
    public ShippingProviderConfigResponse getDefaultConfig(String providerCode) {
        ShippingProviderConfig config = configMapper.findDefaultConfigByProviderCode(providerCode)
                .orElseThrow(() -> new AppException(ErrorCode.PROVIDER_CONFIG_NOT_FOUND, 
                        "Không tìm thấy cấu hình mặc định cho nhà vận chuyển: " + providerCode));
        ShippingProvider provider = providerMapper.selectById(config.getProviderId());
        return toConfigResponse(config, provider);
    }

    @Override
    @Transactional
    public ShippingProviderConfigResponse createConfig(CreateProviderConfigRequest request) {
        ShippingProvider provider = providerMapper.selectById(request.getProviderId());
        if (provider == null) {
            throw new AppException(ErrorCode.PROVIDER_NOT_FOUND);
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            // Unset previous default configs for this provider
            List<ShippingProviderConfig> oldDefaults = configMapper.selectList(
                    new LambdaQueryWrapper<ShippingProviderConfig>()
                            .eq(ShippingProviderConfig::getProviderId, request.getProviderId())
                            .eq(ShippingProviderConfig::getIsDefault, true)
            );
            for (ShippingProviderConfig old : oldDefaults) {
                old.setIsDefault(false);
                configMapper.updateById(old);
            }
        }

        ShippingProviderConfig config = ShippingProviderConfig.builder()
                .providerId(request.getProviderId())
                .tenantId(request.getTenantId())
                .warehouseId(request.getWarehouseId())
                .configName(request.getConfigName())
                .environment(request.getEnvironment() != null ? request.getEnvironment() : "PRODUCTION")
                .apiBaseUrl(request.getApiBaseUrl())
                .token(request.getToken())
                .apiKey(request.getApiKey())
                .secretKey(request.getSecretKey())
                .clientId(request.getClientId())
                .providerShopId(request.getProviderShopId())
                .providerAccountName(request.getProviderAccountName())
                .defaultPickupName(request.getDefaultPickupName())
                .defaultPickupPhone(request.getDefaultPickupPhone())
                .defaultPickupAddress(request.getDefaultPickupAddress())
                .defaultPickupWard(request.getDefaultPickupWard())
                .defaultPickupDistrict(request.getDefaultPickupDistrict())
                .defaultPickupProvince(request.getDefaultPickupProvince())
                .webhookUrl(request.getWebhookUrl())
                .webhookSecret(request.getWebhookSecret())
                .defaultServiceId(request.getDefaultServiceId())
                .defaultPayer(request.getDefaultPayer() != null ? request.getDefaultPayer() : "SHOP")
                .defaultInsuranceEnabled(request.getDefaultInsuranceEnabled() != null ? request.getDefaultInsuranceEnabled() : false)
                .defaultWeightGram(request.getDefaultWeightGram())
                .extraSettings(request.getExtraSettings())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        configMapper.insert(config);
        return toConfigResponse(config, provider);
    }

    @Override
    @Transactional
    public ShippingProviderConfigResponse updateConfig(Long configId, UpdateProviderConfigRequest request) {
        ShippingProviderConfig config = configMapper.selectById(configId);
        if (config == null) {
            throw new AppException(ErrorCode.PROVIDER_CONFIG_NOT_FOUND);
        }

        if (request.getTenantId() != null) config.setTenantId(request.getTenantId());
        if (request.getWarehouseId() != null) config.setWarehouseId(request.getWarehouseId());
        if (request.getConfigName() != null) config.setConfigName(request.getConfigName());
        if (request.getEnvironment() != null) config.setEnvironment(request.getEnvironment());
        if (request.getApiBaseUrl() != null) config.setApiBaseUrl(request.getApiBaseUrl());
        if (request.getToken() != null) config.setToken(request.getToken());
        if (request.getApiKey() != null) config.setApiKey(request.getApiKey());
        if (request.getSecretKey() != null) config.setSecretKey(request.getSecretKey());
        if (request.getClientId() != null) config.setClientId(request.getClientId());
        if (request.getProviderShopId() != null) config.setProviderShopId(request.getProviderShopId());
        if (request.getProviderAccountName() != null) config.setProviderAccountName(request.getProviderAccountName());
        if (request.getDefaultPickupName() != null) config.setDefaultPickupName(request.getDefaultPickupName());
        if (request.getDefaultPickupPhone() != null) config.setDefaultPickupPhone(request.getDefaultPickupPhone());
        if (request.getDefaultPickupAddress() != null) config.setDefaultPickupAddress(request.getDefaultPickupAddress());
        if (request.getDefaultPickupWard() != null) config.setDefaultPickupWard(request.getDefaultPickupWard());
        if (request.getDefaultPickupDistrict() != null) config.setDefaultPickupDistrict(request.getDefaultPickupDistrict());
        if (request.getDefaultPickupProvince() != null) config.setDefaultPickupProvince(request.getDefaultPickupProvince());
        if (request.getWebhookUrl() != null) config.setWebhookUrl(request.getWebhookUrl());
        if (request.getWebhookSecret() != null) config.setWebhookSecret(request.getWebhookSecret());
        if (request.getDefaultServiceId() != null) config.setDefaultServiceId(request.getDefaultServiceId());
        if (request.getDefaultPayer() != null) config.setDefaultPayer(request.getDefaultPayer());
        if (request.getDefaultInsuranceEnabled() != null) config.setDefaultInsuranceEnabled(request.getDefaultInsuranceEnabled());
        if (request.getDefaultWeightGram() != null) config.setDefaultWeightGram(request.getDefaultWeightGram());
        if (request.getExtraSettings() != null) config.setExtraSettings(request.getExtraSettings());
        if (request.getIsDefault() != null) config.setIsDefault(request.getIsDefault());
        if (request.getIsActive() != null) config.setIsActive(request.getIsActive());
        config.setUpdatedAt(LocalDateTime.now());

        configMapper.updateById(config);
        ShippingProvider provider = providerMapper.selectById(config.getProviderId());
        return toConfigResponse(config, provider);
    }

    @Override
    public List<ShippingProviderConfigResponse> getSellerConfigs(Long tenantId) {
        List<ShippingProviderConfig> configs = configMapper.findByTenantId(tenantId);
        return configs.stream().map(c -> {
            ShippingProvider provider = providerMapper.selectById(c.getProviderId());
            return toConfigResponse(c, provider);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShippingProviderConfigResponse saveSellerConfig(Long tenantId, SaveSellerShippingConfigRequest request) {
        ShippingProvider provider = providerMapper.findByCode(request.getProviderCode().toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.PROVIDER_NOT_FOUND, "Không tìm thấy nhà vận chuyển: " + request.getProviderCode()));

        // Resolve service if serviceCode is provided
        Long serviceId = request.getDefaultServiceId();
        if (serviceId == null && StringUtils.hasText(request.getDefaultServiceCode())) {
            com.vn.shipment.web.provider.domain.entity.ShippingProviderService s = serviceMapper
                    .findByProviderIdAndServiceCode(provider.getId(), request.getDefaultServiceCode()).orElse(null);
            if (s != null) serviceId = s.getId();
        }

        // Build extraSettings JSON from typed setting DTOs if provided
        String extraSettings = request.getExtraSettingsJson();
        try {
            if (request.getGhnSettings() != null) {
                extraSettings = objectMapper.writeValueAsString(request.getGhnSettings());
            } else if (request.getGhtkSettings() != null) {
                extraSettings = objectMapper.writeValueAsString(request.getGhtkSettings());
            } else if (request.getAhamoveSettings() != null) {
                extraSettings = objectMapper.writeValueAsString(request.getAhamoveSettings());
            } else if (request.getGoshipSettings() != null) {
                extraSettings = objectMapper.writeValueAsString(request.getGoshipSettings());
            }
        } catch (Exception e) {
            log.error("Failed to serialize extraSettings: {}", e.getMessage());
        }

        Optional<ShippingProviderConfig> existingConfig = configMapper.findByTenantIdAndProviderId(tenantId, provider.getId());
        ShippingProviderConfig config;

        if (existingConfig.isPresent()) {
            config = existingConfig.get();
            if (request.getConfigName() != null) config.setConfigName(request.getConfigName());
            if (request.getEnvironment() != null) config.setEnvironment(request.getEnvironment());
            if (request.getToken() != null) config.setToken(request.getToken());
            if (request.getApiKey() != null) config.setApiKey(request.getApiKey());
            if (request.getSecretKey() != null) config.setSecretKey(request.getSecretKey());
            if (request.getClientId() != null) config.setClientId(request.getClientId());
            if (request.getProviderShopId() != null) config.setProviderShopId(request.getProviderShopId());
            if (request.getProviderAccountName() != null) config.setProviderAccountName(request.getProviderAccountName());
            if (request.getDefaultPickupName() != null) config.setDefaultPickupName(request.getDefaultPickupName());
            if (request.getDefaultPickupPhone() != null) config.setDefaultPickupPhone(request.getDefaultPickupPhone());
            if (request.getDefaultPickupAddress() != null) config.setDefaultPickupAddress(request.getDefaultPickupAddress());
            if (request.getDefaultPickupWard() != null) config.setDefaultPickupWard(request.getDefaultPickupWard());
            if (request.getDefaultPickupDistrict() != null) config.setDefaultPickupDistrict(request.getDefaultPickupDistrict());
            if (request.getDefaultPickupProvince() != null) config.setDefaultPickupProvince(request.getDefaultPickupProvince());
            if (serviceId != null) config.setDefaultServiceId(serviceId);
            if (request.getDefaultPayer() != null) config.setDefaultPayer(request.getDefaultPayer());
            if (request.getDefaultInsuranceEnabled() != null) config.setDefaultInsuranceEnabled(request.getDefaultInsuranceEnabled());
            if (request.getDefaultWeightGram() != null) config.setDefaultWeightGram(request.getDefaultWeightGram());
            if (extraSettings != null) config.setExtraSettings(extraSettings);
            if (request.getIsDefault() != null) config.setIsDefault(request.getIsDefault());
            if (request.getIsActive() != null) config.setIsActive(request.getIsActive());
            config.setUpdatedAt(LocalDateTime.now());
            configMapper.updateById(config);
        } else {
            config = ShippingProviderConfig.builder()
                    .providerId(provider.getId())
                    .tenantId(tenantId)
                    .configName(StringUtils.hasText(request.getConfigName()) ? request.getConfigName() : provider.getName() + " - Gian hàng")
                    .environment(request.getEnvironment() != null ? request.getEnvironment() : "PRODUCTION")
                    .token(request.getToken())
                    .apiKey(request.getApiKey())
                    .secretKey(request.getSecretKey())
                    .clientId(request.getClientId())
                    .providerShopId(request.getProviderShopId())
                    .providerAccountName(request.getProviderAccountName())
                    .defaultPickupName(request.getDefaultPickupName())
                    .defaultPickupPhone(request.getDefaultPickupPhone())
                    .defaultPickupAddress(request.getDefaultPickupAddress())
                    .defaultPickupWard(request.getDefaultPickupWard())
                    .defaultPickupDistrict(request.getDefaultPickupDistrict())
                    .defaultPickupProvince(request.getDefaultPickupProvince())
                    .defaultServiceId(serviceId)
                    .defaultPayer(request.getDefaultPayer() != null ? request.getDefaultPayer() : "SHOP")
                    .defaultInsuranceEnabled(request.getDefaultInsuranceEnabled() != null ? request.getDefaultInsuranceEnabled() : false)
                    .defaultWeightGram(request.getDefaultWeightGram() != null ? request.getDefaultWeightGram() : 500)
                    .extraSettings(extraSettings)
                    .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                    .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            configMapper.insert(config);
        }

        return toConfigResponse(config, provider);
    }

    @Override
    public List<ShippingProviderServiceResponse> getServicesByProviderId(Long providerId) {
        List<com.vn.shipment.web.provider.domain.entity.ShippingProviderService> services = serviceMapper.findByProviderId(providerId);
        return services.stream().map(this::toServiceResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShippingProviderServiceResponse createService(CreateProviderServiceRequest request) {
        ShippingProvider provider = providerMapper.selectById(request.getProviderId());
        if (provider == null) {
            throw new AppException(ErrorCode.PROVIDER_NOT_FOUND);
        }

        if (serviceMapper.findByProviderIdAndServiceCode(request.getProviderId(), request.getServiceCode()).isPresent()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Gói dịch vụ đã tồn tại cho nhà vận chuyển này");
        }

        com.vn.shipment.web.provider.domain.entity.ShippingProviderService entity = com.vn.shipment.web.provider.domain.entity.ShippingProviderService.builder()
                .providerId(request.getProviderId())
                .serviceCode(request.getServiceCode())
                .serviceName(request.getServiceName())
                .maxWeightKg(request.getMaxWeightKg())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        serviceMapper.insert(entity);
        return toServiceResponse(entity);
    }

    private ShippingProviderResponse toProviderResponse(ShippingProvider p) {
        List<ShippingProviderServiceResponse> services = serviceMapper.findByProviderId(p.getId())
                .stream().map(this::toServiceResponse).collect(Collectors.toList());

        return ShippingProviderResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .logoUrl(p.getLogoUrl())
                .supportsCod(p.getSupportsCod())
                .supportsInsurance(p.getSupportsInsurance())
                .supportsWebhook(p.getSupportsWebhook())
                .trackingUrlTemplate(p.getTrackingUrlTemplate())
                .isActive(p.getIsActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .services(services)
                .build();
    }

    private ShippingProviderConfigResponse toConfigResponse(ShippingProviderConfig c, ShippingProvider p) {
        com.vn.shipment.web.provider.domain.entity.ShippingProviderService s = c.getDefaultServiceId() != null 
                ? serviceMapper.selectById(c.getDefaultServiceId()) : null;

        return ShippingProviderConfigResponse.builder()
                .id(c.getId())
                .providerId(c.getProviderId())
                .providerCode(p != null ? p.getCode() : null)
                .providerName(p != null ? p.getName() : null)
                .tenantId(c.getTenantId())
                .warehouseId(c.getWarehouseId())
                .configName(c.getConfigName())
                .environment(c.getEnvironment())
                .apiBaseUrl(c.getApiBaseUrl())
                .token(c.getToken())
                .clientId(c.getClientId())
                .providerShopId(c.getProviderShopId())
                .providerAccountName(c.getProviderAccountName())
                .defaultPickupName(c.getDefaultPickupName())
                .defaultPickupPhone(c.getDefaultPickupPhone())
                .defaultPickupAddress(c.getDefaultPickupAddress())
                .defaultPickupWard(c.getDefaultPickupWard())
                .defaultPickupDistrict(c.getDefaultPickupDistrict())
                .defaultPickupProvince(c.getDefaultPickupProvince())
                .webhookUrl(c.getWebhookUrl())
                .defaultServiceId(c.getDefaultServiceId())
                .defaultServiceCode(s != null ? s.getServiceCode() : null)
                .defaultServiceName(s != null ? s.getServiceName() : null)
                .defaultPayer(c.getDefaultPayer())
                .defaultInsuranceEnabled(c.getDefaultInsuranceEnabled())
                .defaultWeightGram(c.getDefaultWeightGram())
                .extraSettings(c.getExtraSettings())
                .isDefault(c.getIsDefault())
                .isActive(c.getIsActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private ShippingProviderServiceResponse toServiceResponse(com.vn.shipment.web.provider.domain.entity.ShippingProviderService s) {
        return ShippingProviderServiceResponse.builder()
                .id(s.getId())
                .providerId(s.getProviderId())
                .serviceCode(s.getServiceCode())
                .serviceName(s.getServiceName())
                .maxWeightKg(s.getMaxWeightKg())
                .isActive(s.getIsActive())
                .build();
    }
}
