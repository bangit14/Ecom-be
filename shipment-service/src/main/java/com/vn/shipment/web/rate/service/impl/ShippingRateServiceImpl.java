package com.vn.shipment.web.rate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vn.shipment.core.exception.AppException;
import com.vn.shipment.core.exception.ErrorCode;
import com.vn.shipment.web.provider.domain.entity.ShippingProvider;
import com.vn.shipment.web.provider.domain.entity.ShippingProviderService;
import com.vn.shipment.web.provider.mapper.ShippingProviderMapper;
import com.vn.shipment.web.provider.mapper.ShippingProviderServiceMapper;
import com.vn.shipment.web.rate.domain.entity.ShippingRate;
import com.vn.shipment.web.rate.domain.request.CalculateShippingFeeRequest;
import com.vn.shipment.web.rate.domain.request.CreateShippingRateRequest;
import com.vn.shipment.web.rate.domain.response.ShippingFeeEstimateResponse;
import com.vn.shipment.web.rate.domain.response.ShippingRateResponse;
import com.vn.shipment.web.rate.mapper.ShippingRateMapper;
import com.vn.shipment.web.rate.service.ShippingRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingRateServiceImpl implements ShippingRateService {

    private final ShippingRateMapper rateMapper;
    private final ShippingProviderMapper providerMapper;
    private final ShippingProviderServiceMapper serviceMapper;

    @Override
    public List<ShippingFeeEstimateResponse> calculateShippingFees(CalculateShippingFeeRequest request) {
        List<ShippingProvider> providers = providerMapper.selectList(
                new LambdaQueryWrapper<ShippingProvider>().eq(ShippingProvider::getIsActive, true)
        );

        List<ShippingFeeEstimateResponse> estimates = new ArrayList<>();

        for (ShippingProvider provider : providers) {
            if (request.getProviderCode() != null && !request.getProviderCode().equalsIgnoreCase(provider.getCode())) {
                continue;
            }

            List<ShippingProviderService> services = serviceMapper.findByProviderId(provider.getId());
            if (services.isEmpty()) {
                estimates.add(buildEstimate(provider, null, request));
            } else {
                for (ShippingProviderService service : services) {
                    if (request.getServiceCode() != null && !request.getServiceCode().equalsIgnoreCase(service.getServiceCode())) {
                        continue;
                    }
                    estimates.add(buildEstimate(provider, service, request));
                }
            }
        }

        return estimates;
    }

    @Override
    public ShippingFeeEstimateResponse calculateFeeForProvider(String providerCode, CalculateShippingFeeRequest request) {
        ShippingProvider provider = providerMapper.findByCode(providerCode)
                .orElseThrow(() -> new AppException(ErrorCode.PROVIDER_NOT_FOUND, "Không tìm thấy nhà vận chuyển: " + providerCode));

        ShippingProviderService service = null;
        if (request.getServiceCode() != null) {
            service = serviceMapper.findByProviderIdAndServiceCode(provider.getId(), request.getServiceCode()).orElse(null);
        }

        return buildEstimate(provider, service, request);
    }

    @Override
    public List<ShippingRateResponse> getAllRates() {
        List<ShippingRate> rates = rateMapper.selectList(
                new LambdaQueryWrapper<ShippingRate>().orderByAsc(ShippingRate::getId)
        );
        return rates.stream().map(this::toRateResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShippingRateResponse createRate(CreateShippingRateRequest request) {
        ShippingProvider provider = providerMapper.selectById(request.getProviderId());
        if (provider == null) {
            throw new AppException(ErrorCode.PROVIDER_NOT_FOUND);
        }

        ShippingRate rate = ShippingRate.builder()
                .providerId(request.getProviderId())
                .serviceId(request.getServiceId())
                .fromProvince(request.getFromProvince())
                .toProvince(request.getToProvince())
                .weightFromGram(request.getWeightFromGram() != null ? request.getWeightFromGram() : 0)
                .weightToGram(request.getWeightToGram() != null ? request.getWeightToGram() : 2000)
                .baseFee(request.getBaseFee())
                .feePerExtraKg(request.getFeePerExtraKg() != null ? request.getFeePerExtraKg() : BigDecimal.ZERO)
                .codFeePercent(request.getCodFeePercent() != null ? request.getCodFeePercent() : BigDecimal.ZERO)
                .insuranceFeePercent(request.getInsuranceFeePercent() != null ? request.getInsuranceFeePercent() : BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdAt(LocalDateTime.now())
                .build();

        rateMapper.insert(rate);
        return toRateResponse(rate);
    }

    @Override
    @Transactional
    public void deleteRate(Long id) {
        rateMapper.deleteById(id);
    }

    private ShippingFeeEstimateResponse buildEstimate(ShippingProvider provider,
                                                      ShippingProviderService service,
                                                      CalculateShippingFeeRequest req) {
        Long serviceId = service != null ? service.getId() : null;
        int weight = req.getWeightGram() != null ? req.getWeightGram() : 500;
        BigDecimal cod = req.getCodAmount() != null ? req.getCodAmount() : BigDecimal.ZERO;
        BigDecimal orderVal = req.getOrderValue() != null ? req.getOrderValue() : BigDecimal.ZERO;

        Optional<ShippingRate> rateOpt = rateMapper.findMatchingRate(
                provider.getId(), serviceId, req.getFromProvince(), req.getToProvince(), weight
        );

        BigDecimal baseFee;
        BigDecimal extraWeightFee = BigDecimal.ZERO;
        BigDecimal codFee = BigDecimal.ZERO;
        BigDecimal insuranceFee = BigDecimal.ZERO;

        boolean isIntraProvince = req.getFromProvince() != null && req.getFromProvince().equalsIgnoreCase(req.getToProvince());

        if (rateOpt.isPresent()) {
            ShippingRate rate = rateOpt.get();
            baseFee = rate.getBaseFee();

            if (rate.getWeightToGram() != null && weight > rate.getWeightToGram() && rate.getFeePerExtraKg().compareTo(BigDecimal.ZERO) > 0) {
                int extraGrams = weight - rate.getWeightToGram();
                int extraKg = (int) Math.ceil((double) extraGrams / 1000.0);
                extraWeightFee = rate.getFeePerExtraKg().multiply(BigDecimal.valueOf(extraKg));
            }

            if (rate.getCodFeePercent().compareTo(BigDecimal.ZERO) > 0) {
                codFee = cod.multiply(rate.getCodFeePercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }

            if (rate.getInsuranceFeePercent().compareTo(BigDecimal.ZERO) > 0) {
                insuranceFee = orderVal.multiply(rate.getInsuranceFeePercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
        } else {
            // Standard fallback rate estimation based on distance
            baseFee = isIntraProvince ? BigDecimal.valueOf(22000) : BigDecimal.valueOf(35000);
            if (weight > 2000) {
                int extraKg = (int) Math.ceil((double) (weight - 2000) / 1000.0);
                extraWeightFee = BigDecimal.valueOf(5000).multiply(BigDecimal.valueOf(extraKg));
            }
        }

        BigDecimal totalFee = baseFee.add(extraWeightFee).add(codFee).add(insuranceFee);
        String estTime = isIntraProvince ? "Trong ngày / 24h" : "2 - 3 ngày";

        return ShippingFeeEstimateResponse.builder()
                .providerCode(provider.getCode())
                .providerName(provider.getName())
                .serviceCode(service != null ? service.getServiceCode() : "STANDARD")
                .serviceName(service != null ? service.getServiceName() : "Giao hàng tiêu chuẩn")
                .logoUrl(provider.getLogoUrl())
                .shippingFee(totalFee)
                .baseFee(baseFee)
                .extraWeightFee(extraWeightFee)
                .codFee(codFee)
                .insuranceFee(insuranceFee)
                .totalFee(totalFee)
                .estimatedDeliveryTime(estTime)
                .currency("VND")
                .build();
    }

    private ShippingRateResponse toRateResponse(ShippingRate r) {
        ShippingProvider p = providerMapper.selectById(r.getProviderId());
        ShippingProviderService s = r.getServiceId() != null ? serviceMapper.selectById(r.getServiceId()) : null;

        return ShippingRateResponse.builder()
                .id(r.getId())
                .providerId(r.getProviderId())
                .providerCode(p != null ? p.getCode() : null)
                .providerName(p != null ? p.getName() : null)
                .serviceId(r.getServiceId())
                .serviceCode(s != null ? s.getServiceCode() : null)
                .serviceName(s != null ? s.getServiceName() : null)
                .fromProvince(r.getFromProvince())
                .toProvince(r.getToProvince())
                .weightFromGram(r.getWeightFromGram())
                .weightToGram(r.getWeightToGram())
                .baseFee(r.getBaseFee())
                .feePerExtraKg(r.getFeePerExtraKg())
                .codFeePercent(r.getCodFeePercent())
                .insuranceFeePercent(r.getInsuranceFeePercent())
                .currency(r.getCurrency())
                .effectiveFrom(r.getEffectiveFrom())
                .effectiveTo(r.getEffectiveTo())
                .isActive(r.getIsActive())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
