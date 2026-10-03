package com.vn.shipment.web.ghn.service;

import com.vn.shipment.web.ghn.domain.dto.*;
import com.vn.shipment.web.ghn.domain.request.GhnAvailableServicesRequest;

import java.util.List;
import java.util.Map;

public interface GhnMasterDataService {

    List<GhnProvinceDto> getProvinces(Long shopId);

    List<GhnDistrictDto> getDistricts(Integer provinceId, Long shopId);

    List<GhnWardDto> getWards(Integer districtId, Long shopId);

    List<GhnServicePackageDto> getAvailableServices(GhnAvailableServicesRequest request, Long shopId);

    List<GhnPickShiftDto> getPickShifts();

    List<GhnStationDto> getStations(Integer districtId);

    Map<String, Object> syncMasterDataToDatabase();
}
