package com.vn.shipment.web.webhook.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarrierWebhookPayload {

    // General fields
    private String providerCode; // GHN, GHTK, AHAMOVE, GOSHIP
    private String trackingNumber;
    private String externalOrderCode;
    private String shipmentCode;
    private String status; // raw carrier status
    private String statusDescription;
    private String reason;
    private String location;
    private String weight;
    private String fee;
    private LocalDateTime eventTime;

    // GHN specific mapping
    @JsonProperty("OrderCode")
    private String ghnOrderCode;

    @JsonProperty("Type")
    private String ghnType;

    @JsonProperty("Status")
    private String ghnStatus;

    @JsonProperty("Description")
    private String ghnDescription;

    @JsonProperty("Time")
    private String ghnTime;

    // GHTK specific mapping
    @JsonProperty("label_id")
    private String ghtkLabelId;

    @JsonProperty("partner_id")
    private String ghtkPartnerId;

    @JsonProperty("status_id")
    private Integer ghtkStatusId;

    @JsonProperty("action_time")
    private String ghtkActionTime;

    @JsonProperty("reason_code")
    private String ghtkReasonCode;
}
