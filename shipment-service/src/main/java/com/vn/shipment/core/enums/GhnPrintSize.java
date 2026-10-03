package com.vn.shipment.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum GhnPrintSize {

    A5("printA5", "Khổ giấy A5 (A4 chia đôi)"),
    SIZE_80x80("print80x80", "Khổ giấy 80x80 mm (máy in nhiệt)"),
    SIZE_52x70("print52x70", "Khổ giấy 52x70 mm (máy in mã vạch)");

    private final String apiPath;
    private final String description;

    GhnPrintSize(String apiPath, String description) {
        this.apiPath = apiPath;
        this.description = description;
    }

    @JsonValue
    public String getApiPath() {
        return apiPath;
    }

    @JsonCreator
    public static GhnPrintSize fromApiPath(String path) {
        if (path == null) return A5;
        for (GhnPrintSize size : values()) {
            if (size.apiPath.equalsIgnoreCase(path) || size.name().equalsIgnoreCase(path)) {
                return size;
            }
        }
        return A5;
    }
}
