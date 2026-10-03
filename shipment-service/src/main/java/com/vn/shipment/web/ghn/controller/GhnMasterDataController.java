package com.vn.shipment.web.ghn.controller;

import com.vn.shipment.core.exception.ApiResponse;
import com.vn.shipment.web.ghn.domain.dto.*;
import com.vn.shipment.web.ghn.domain.request.GhnAvailableServicesRequest;
import com.vn.shipment.web.ghn.service.GhnMasterDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shipment/ghn/master-data")
@RequiredArgsConstructor
public class GhnMasterDataController {

    private final GhnMasterDataService masterDataService;

    @GetMapping("/provinces")
    public ApiResponse<List<GhnProvinceDto>> getProvinces(@RequestParam(value = "shop_id", required = false) Long shopId) {
        return ApiResponse.success(masterDataService.getProvinces(shopId));
    }

    @GetMapping("/districts")
    public ApiResponse<List<GhnDistrictDto>> getDistricts(
            @RequestParam("province_id") Integer provinceId,
            @RequestParam(value = "shop_id", required = false) Long shopId) {
        return ApiResponse.success(masterDataService.getDistricts(provinceId, shopId));
    }

    @GetMapping("/wards")
    public ApiResponse<List<GhnWardDto>> getWards(
            @RequestParam("district_id") Integer districtId,
            @RequestParam(value = "shop_id", required = false) Long shopId) {
        return ApiResponse.success(masterDataService.getWards(districtId, shopId));
    }

    @PostMapping("/available-services")
    public ApiResponse<List<GhnServicePackageDto>> getAvailableServices(
            @Valid @RequestBody GhnAvailableServicesRequest request,
            @RequestParam(value = "shop_id", required = false) Long shopId) {
        return ApiResponse.success(masterDataService.getAvailableServices(request, shopId));
    }

    @GetMapping("/pick-shifts")
    public ApiResponse<List<GhnPickShiftDto>> getPickShifts() {
        return ApiResponse.success(masterDataService.getPickShifts());
    }

    @GetMapping("/stations")
    public ApiResponse<List<GhnStationDto>> getStations(
            @RequestParam(value = "district_id", required = false) Integer districtId) {
        return ApiResponse.success(masterDataService.getStations(districtId));
    }

    @PostMapping("/sync")
    public ApiResponse<Map<String, Object>> syncMasterData() {
        return ApiResponse.success("Đồng bộ dữ liệu hành chính GHN thành công", masterDataService.syncMasterDataToDatabase());
    }
}
