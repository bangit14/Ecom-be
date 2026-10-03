package com.vn.shipment.web.ghn.service.impl;

import com.vn.shipment.core.exception.AppException;
import com.vn.shipment.core.exception.ErrorCode;
import com.vn.shipment.web.address.domain.entity.ShippingDistrict;
import com.vn.shipment.web.address.domain.entity.ShippingProvince;
import com.vn.shipment.web.address.domain.entity.ShippingWard;
import com.vn.shipment.web.address.mapper.ShippingDistrictMapper;
import com.vn.shipment.web.address.mapper.ShippingProvinceMapper;
import com.vn.shipment.web.address.mapper.ShippingWardMapper;
import com.vn.shipment.web.ghn.client.GhnApiClient;
import com.vn.shipment.web.ghn.domain.dto.*;
import com.vn.shipment.web.ghn.domain.request.GhnAvailableServicesRequest;
import com.vn.shipment.web.ghn.service.GhnMasterDataService;
import com.vn.shipment.web.provider.domain.entity.ShippingProvider;
import com.vn.shipment.web.provider.mapper.ShippingProviderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GhnMasterDataServiceImpl implements GhnMasterDataService {

    private final GhnApiClient ghnApiClient;
    private final ShippingProviderMapper providerMapper;
    private final ShippingProvinceMapper provinceMapper;
    private final ShippingDistrictMapper districtMapper;
    private final ShippingWardMapper wardMapper;

    private ShippingProvider getGhnProvider() {
        return providerMapper.findByCode("GHN")
                .orElseThrow(() -> new AppException(ErrorCode.PROVIDER_NOT_FOUND, "Không tìm thấy cấu hình nhà vận chuyển GHN"));
    }

    @Override
    public List<GhnProvinceDto> getProvinces(Long shopId) {
        return ghnApiClient.getProvinces(null, shopId);
    }

    @Override
    public List<GhnDistrictDto> getDistricts(Integer provinceId, Long shopId) {
        if (provinceId == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "provinceId không được để trống");
        }
        return ghnApiClient.getDistricts(provinceId, null, shopId);
    }

    @Override
    public List<GhnWardDto> getWards(Integer districtId, Long shopId) {
        if (districtId == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "districtId không được để trống");
        }
        return ghnApiClient.getWards(districtId, null, shopId);
    }

    @Override
    public List<GhnServicePackageDto> getAvailableServices(GhnAvailableServicesRequest request, Long shopId) {
        if (request.getFromDistrict() == null || request.getToDistrict() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "from_district và to_district không được để trống");
        }
        return ghnApiClient.getAvailableServices(request, null, shopId);
    }

    @Override
    public List<GhnPickShiftDto> getPickShifts() {
        return ghnApiClient.getPickShifts(null);
    }

    @Override
    public List<GhnStationDto> getStations(Integer districtId) {
        return ghnApiClient.getStations(districtId, null);
    }

    @Override
    @Transactional
    public Map<String, Object> syncMasterDataToDatabase() {
        ShippingProvider ghn = getGhnProvider();
        Long providerId = ghn.getId();

        log.info("Starting master data sync from GHN for provider ID: {}", providerId);
        List<GhnProvinceDto> provinces = ghnApiClient.getProvinces(null, null);

        int provinceCount = 0;
        int districtCount = 0;

        for (GhnProvinceDto pDto : provinces) {
            if (pDto.getProvinceId() == null) continue;
            String pExtCode = String.valueOf(pDto.getProvinceId());

            Optional<ShippingProvince> existingPOpt = provinceMapper.findByProviderIdAndExternalCode(providerId, pExtCode);
            ShippingProvince province;
            if (existingPOpt.isPresent()) {
                province = existingPOpt.get();
                province.setProvinceName(pDto.getProvinceName());
                province.setStandardCode(pDto.getCode());
                provinceMapper.updateById(province);
            } else {
                province = ShippingProvince.builder()
                        .providerId(providerId)
                        .externalCode(pExtCode)
                        .provinceName(pDto.getProvinceName())
                        .standardCode(pDto.getCode())
                        .build();
                provinceMapper.insert(province);
            }
            provinceCount++;

            // Sync top major provinces' districts directly or all
            try {
                List<GhnDistrictDto> districts = ghnApiClient.getDistricts(pDto.getProvinceId(), null, null);
                for (GhnDistrictDto dDto : districts) {
                    if (dDto.getDistrictId() == null) continue;
                    String dExtCode = String.valueOf(dDto.getDistrictId());

                    Optional<ShippingDistrict> existingDOpt = districtMapper.findByProviderIdAndExternalCode(providerId, dExtCode);
                    if (existingDOpt.isPresent()) {
                        ShippingDistrict district = existingDOpt.get();
                        district.setDistrictName(dDto.getDistrictName());
                        district.setProvinceRefId(province.getId());
                        district.setStandardCode(dDto.getCode());
                        districtMapper.updateById(district);
                    } else {
                        ShippingDistrict district = ShippingDistrict.builder()
                                .providerId(providerId)
                                .provinceRefId(province.getId())
                                .externalCode(dExtCode)
                                .districtName(dDto.getDistrictName())
                                .standardCode(dDto.getCode())
                                .build();
                        districtMapper.insert(district);
                    }
                    districtCount++;
                }
            } catch (Exception e) {
                log.warn("Could not sync districts for province {}: {}", pDto.getProvinceName(), e.getMessage());
            }
        }

        log.info("Finished GHN master data sync. Provinces: {}, Districts: {}", provinceCount, districtCount);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "SUCCESS");
        result.put("provincesSynced", provinceCount);
        result.put("districtsSynced", districtCount);
        return result;
    }
}
