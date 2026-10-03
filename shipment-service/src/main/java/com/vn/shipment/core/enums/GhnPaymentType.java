package com.vn.shipment.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum GhnPaymentType {

    SHOP(1, "Shop / Người bán trả phí vận chuyển"),
    BUYER(2, "Người mua / Người nhận trả phí vận chuyển");

    private final int id;
    private final String description;

    GhnPaymentType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    @JsonValue
    public int getId() {
        return id;
    }

    @JsonCreator
    public static GhnPaymentType fromId(Integer id) {
        if (id == null) return null;
        for (GhnPaymentType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return SHOP;
    }
}
