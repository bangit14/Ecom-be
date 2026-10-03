package com.vn.shipment.web.webhook.service;

import com.vn.shipment.web.shipment.domain.response.ShipmentDetailResponse;
import com.vn.shipment.web.webhook.domain.request.CarrierWebhookPayload;

public interface ShipmentWebhookService {

    ShipmentDetailResponse handleCarrierWebhook(String providerCode, String payloadJson, CarrierWebhookPayload payload);
}
