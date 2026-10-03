package com.vn.shipment.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum GhnOrderStatus {

    READY_TO_PICK("ready_to_pick", "Mới tạo đơn, chờ GHN đến lấy hàng", ShipmentStatus.READY_TO_PICK, false),
    PICKING("picking", "Tài xế đang đến lấy hàng", ShipmentStatus.PICKING, false),
    MONEY_COLLECT_PICKING("money_collect_picking", "Thu tiền khi lấy hàng", ShipmentStatus.PICKING, false),
    PICKED("picked", "Đã lấy hàng từ người gửi", ShipmentStatus.IN_TRANSIT, false),
    STORING("storing", "Hàng đang lưu tại kho GHN", ShipmentStatus.IN_TRANSIT, false),
    SORTING("sorting", "Đang phân loại tại kho GHN", ShipmentStatus.IN_TRANSIT, false),
    TRANSPORTING("transporting", "Đang luân chuyển hàng giữa các kho", ShipmentStatus.IN_TRANSIT, false),
    DELIVERING("delivering", "Tài xế đang đi giao cho người nhận", ShipmentStatus.IN_TRANSIT, false),
    MONEY_COLLECT_DELIVERING("money_collect_delivering", "Đang thu tiền khi giao hàng", ShipmentStatus.IN_TRANSIT, false),
    DELIVERED("delivered", "Giao hàng thành công", ShipmentStatus.DELIVERED, true),
    DELIVERY_FAIL("delivery_fail", "Giao hàng không thành công", ShipmentStatus.DELIVERY_FAILED, false),
    WAITING_TO_RETURN("waiting_to_return", "Chờ xác nhận giao lại hoặc chuyển hoàn", ShipmentStatus.IN_TRANSIT, false),
    RETURN("return", "Đang trong quy trình chuyển hoàn", ShipmentStatus.IN_TRANSIT, false),
    RETURN_TRANSPORTING("return_transporting", "Đang luân chuyển hàng hoàn giữa các kho", ShipmentStatus.IN_TRANSIT, false),
    RETURN_SORTING("return_sorting", "Đang phân loại hàng hoàn", ShipmentStatus.IN_TRANSIT, false),
    RETURNING("returning", "Tài xế đang trả lại hàng cho người gửi", ShipmentStatus.IN_TRANSIT, false),
    RETURN_FAIL("return_fail", "Trả lại hàng cho người gửi thất bại", ShipmentStatus.IN_TRANSIT, false),
    RETURNED("returned", "Đã trả hàng cho người gửi thành công", ShipmentStatus.RETURNED, true),
    CANCEL("cancel", "Đơn hàng đã bị hủy", ShipmentStatus.CANCELLED, true),
    EXCEPTION("exception", "Hàng ngoại lệ, đang xử lý tại GHN", ShipmentStatus.IN_TRANSIT, false),
    DAMAGE("damage", "Hàng hóa bị hư hỏng trong quá trình vận chuyển", ShipmentStatus.DELIVERY_FAILED, true),
    LOST("lost", "Hàng hóa bị thất lạc", ShipmentStatus.DELIVERY_FAILED, true);

    private final String code;
    private final String description;
    private final ShipmentStatus internalStatus;
    private final boolean isTerminal;

    GhnOrderStatus(String code, String description, ShipmentStatus internalStatus, boolean isTerminal) {
        this.code = code;
        this.description = description;
        this.internalStatus = internalStatus;
        this.isTerminal = isTerminal;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static GhnOrderStatus fromCode(String code) {
        if (code == null) return null;
        for (GhnOrderStatus s : values()) {
            if (s.code.equalsIgnoreCase(code) || s.name().equalsIgnoreCase(code)) {
                return s;
            }
        }
        return null;
    }

    public ShipmentStatus toShipmentStatus() {
        return internalStatus != null ? internalStatus : ShipmentStatus.IN_TRANSIT;
    }
}
