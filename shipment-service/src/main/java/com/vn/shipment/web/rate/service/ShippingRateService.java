package com.vn.shipment.web.rate.service;

import com.vn.shipment.web.rate.domain.request.CalculateShippingFeeRequest;
import com.vn.shipment.web.rate.domain.request.CreateShippingRateRequest;
import com.vn.shipment.web.rate.domain.response.ShippingFeeEstimateResponse;
import com.vn.shipment.web.rate.domain.response.ShippingRateResponse;

import java.util.List;

public interface ShippingRateService {

    List<ShippingFeeEstimateResponse> calculateShippingFees(CalculateShippingFeeRequest request);

    ShippingFeeEstimateResponse calculateFeeForProvider(String providerCode, CalculateShippingFeeRequest request);

    List<ShippingRateResponse> getAllRates();

    ShippingRateResponse createRate(CreateShippingRateRequest request);

    void deleteRate(Long id);
}
