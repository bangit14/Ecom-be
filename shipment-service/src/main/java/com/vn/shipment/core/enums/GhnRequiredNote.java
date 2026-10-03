package com.vn.shipment.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum GhnRequiredNote {

    CHOTHUHANG("CHOTHUHANG", "Cho thử hàng"),
    CHOXEMHANGKHONGTHU("CHOXEMHANGKHONGTHU", "Cho xem hàng không cho thử"),
    KHONGCHOXEMHANG("KHONGCHOXEMHANG", "Không cho xem hàng");

    private final String code;
    private final String description;

    GhnRequiredNote(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static GhnRequiredNote fromCode(String code) {
        if (code == null) return null;
        for (GhnRequiredNote item : values()) {
            if (item.code.equalsIgnoreCase(code) || item.name().equalsIgnoreCase(code)) {
                return item;
            }
        }
        return CHOXEMHANGKHONGTHU; // default fallback
    }
}
