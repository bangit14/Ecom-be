package com.vn.shipment.web.address.service;

import com.vn.shipment.web.address.domain.response.ShippingDistrictResponse;
import com.vn.shipment.web.address.domain.response.ShippingProvinceResponse;
import com.vn.shipment.web.address.domain.response.ShippingWardResponse;

import java.util.List;

public interface ShippingAddressService {

    List<ShippingProvinceResponse> getProvincesByProviderId(Long providerId);

    List<ShippingProvinceResponse> getProvincesByProviderCode(String providerCode);

    List<ShippingDistrictResponse> getDistrictsByProvinceId(Long provinceRefId);

    List<ShippingWardResponse> getWardsByDistrictId(Long districtRefId);
}
