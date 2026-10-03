package com.vn.shipment.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum GhnReasonCode {

    GHN_CO001("GHN-CO001", "Thời gian lấy hàng quá lâu"),
    GHN_CO002("GHN-CO002", "Hết hàng, chưa có hàng để gửi"),
    GHN_CO003("GHN-CO003", "Người nhận không còn nhu cầu nữa"),
    GHN_CANCEL_OTHER("GHN-CANCEL-OTHER", "Lý do khác");

    private final String code;
    private final String description;

    GhnReasonCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static GhnReasonCode fromCode(String code) {
        if (code == null) return null;
        for (GhnReasonCode r : values()) {
            if (r.code.equalsIgnoreCase(code) || r.name().equalsIgnoreCase(code)) {
                return r;
            }
        }
        return GHN_CANCEL_OTHER;
    }
}
