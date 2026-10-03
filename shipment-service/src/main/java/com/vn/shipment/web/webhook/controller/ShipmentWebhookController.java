package com.vn.shipment.web.webhook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.shipment.core.exception.ApiResponse;
import com.vn.shipment.web.shipment.domain.response.ShipmentDetailResponse;
import com.vn.shipment.web.webhook.domain.request.CarrierWebhookPayload;
import com.vn.shipment.web.webhook.service.ShipmentWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipments/webhook")
@RequiredArgsConstructor
@Slf4j
public class ShipmentWebhookController {

    private final ShipmentWebhookService webhookService;
    private final ObjectMapper objectMapper;

    @PostMapping("/{providerCode}")
    public ApiResponse<ShipmentDetailResponse> handleWebhook(
            @PathVariable String providerCode,
            @RequestBody String rawJson) {
        try {
            CarrierWebhookPayload payload = objectMapper.readValue(rawJson, CarrierWebhookPayload.class);
            payload.setProviderCode(providerCode);
            ShipmentDetailResponse response = webhookService.handleCarrierWebhook(providerCode, rawJson, payload);
            return ApiResponse.success("Webhook đã xử lý thành công", response);
        } catch (Exception e) {
            log.error("Error processing carrier webhook from [{}]: {}", providerCode, e.getMessage(), e);
            return ApiResponse.error(500, "WEBHOOK_PROCESS_ERROR", "Lỗi khi xử lý webhook: " + e.getMessage());
        }
    }
}
