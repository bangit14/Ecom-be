package com.vn.shipment.web.ghn.service;

import com.vn.shipment.web.ghn.domain.dto.GhnFeeDetailDto;
import com.vn.shipment.web.ghn.domain.request.*;
import com.vn.shipment.web.ghn.domain.response.GhnCreateOrderResponse;
import com.vn.shipment.web.ghn.domain.response.GhnPrintOrderResponse;
import com.vn.shipment.web.shipment.domain.request.CreateShipmentRequest;
import com.vn.shipment.web.shipment.domain.response.ShipmentDetailResponse;

import java.time.LocalDateTime;

public interface GhnOrderService {

    GhnCreateOrderResponse createOrder(GhnCreateOrderRequest request, Long shopId);

    GhnFeeDetailDto calculateFee(GhnCalculateFeeRequest request, Long shopId);

    LocalDateTime calculateLeadtime(GhnLeadtimeRequest request, Long shopId);

    void cancelOrder(GhnCancelOrderRequest request, Long shopId);

    GhnPrintOrderResponse generatePrintToken(GhnPrintOrderRequest request, Long shopId);

    ShipmentDetailResponse createShipmentWithGhn(CreateShipmentRequest request);
}
