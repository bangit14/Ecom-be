package com.vn.shipment.web.address.service.impl;

import com.vn.shipment.core.exception.AppException;
import com.vn.shipment.core.exception.ErrorCode;
import com.vn.shipment.web.address.domain.entity.ShippingDistrict;
import com.vn.shipment.web.address.domain.entity.ShippingProvince;
import com.vn.shipment.web.address.domain.entity.ShippingWard;
import com.vn.shipment.web.address.domain.response.ShippingDistrictResponse;
import com.vn.shipment.web.address.domain.response.ShippingProvinceResponse;
import com.vn.shipment.web.address.domain.response.ShippingWardResponse;
import com.vn.shipment.web.address.mapper.ShippingDistrictMapper;
import com.vn.shipment.web.address.mapper.ShippingProvinceMapper;
import com.vn.shipment.web.address.mapper.ShippingWardMapper;
import com.vn.shipment.web.address.service.ShippingAddressService;
import com.vn.shipment.web.provider.domain.entity.ShippingProvider;
import com.vn.shipment.web.provider.mapper.ShippingProviderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingAddressServiceImpl implements ShippingAddressService {

    private final ShippingProvinceMapper provinceMapper;
    private final ShippingDistrictMapper districtMapper;
    private final ShippingWardMapper wardMapper;
    private final ShippingProviderMapper providerMapper;

    @Override
    public List<ShippingProvinceResponse> getProvincesByProviderId(Long providerId) {
        List<ShippingProvince> list = provinceMapper.findByProviderId(providerId);
        return list.stream().map(this::toProvinceResponse).collect(Collectors.toList());
    }

    @Override
    public List<ShippingProvinceResponse> getProvincesByProviderCode(String providerCode) {
        ShippingProvider provider = providerMapper.findByCode(providerCode)
                .orElseThrow(() -> new AppException(ErrorCode.PROVIDER_NOT_FOUND, "Không tìm thấy nhà vận chuyển: " + providerCode));
        return getProvincesByProviderId(provider.getId());
    }

    @Override
    public List<ShippingDistrictResponse> getDistrictsByProvinceId(Long provinceRefId) {
        List<ShippingDistrict> list = districtMapper.findByProvinceRefId(provinceRefId);
        return list.stream().map(this::toDistrictResponse).collect(Collectors.toList());
    }

    @Override
    public List<ShippingWardResponse> getWardsByDistrictId(Long districtRefId) {
        List<ShippingWard> list = wardMapper.findByDistrictRefId(districtRefId);
        return list.stream().map(this::toWardResponse).collect(Collectors.toList());
    }

    private ShippingProvinceResponse toProvinceResponse(ShippingProvince p) {
        return ShippingProvinceResponse.builder()
                .id(p.getId())
                .providerId(p.getProviderId())
                .externalCode(p.getExternalCode())
                .provinceName(p.getProvinceName())
                .standardCode(p.getStandardCode())
                .build();
    }

    private ShippingDistrictResponse toDistrictResponse(ShippingDistrict d) {
        return ShippingDistrictResponse.builder()
                .id(d.getId())
                .providerId(d.getProviderId())
                .provinceRefId(d.getProvinceRefId())
                .externalCode(d.getExternalCode())
                .districtName(d.getDistrictName())
                .standardCode(d.getStandardCode())
                .build();
    }

    private ShippingWardResponse toWardResponse(ShippingWard w) {
        return ShippingWardResponse.builder()
                .id(w.getId())
                .providerId(w.getProviderId())
                .districtRefId(w.getDistrictRefId())
                .externalCode(w.getExternalCode())
                .wardName(w.getWardName())
                .standardCode(w.getStandardCode())
                .build();
    }
}
