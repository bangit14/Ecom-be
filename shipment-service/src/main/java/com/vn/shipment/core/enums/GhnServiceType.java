package com.vn.shipment.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum GhnServiceType {

    EXPRESS(1, "Chuyển phát nhanh GHN"),
    STANDARD(2, "Chuẩn - Hàng nhẹ (tổng khối lượng dưới 20kg)"),
    SAVING(3, "Tiết kiệm GHN"),
    HEAVY_OR_BULKY(5, "Hàng nặng / cồng kềnh (từ 20kg trở lên hoặc đơn nhiều kiện)");

    private final int id;
    private final String description;

    GhnServiceType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    @JsonValue
    public int getId() {
        return id;
    }

    @JsonCreator
    public static GhnServiceType fromId(Integer id) {
        if (id == null) return null;
        for (GhnServiceType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return STANDARD;
    }
}
