package com.vn.shipment.core.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_REQUEST(400, "INVALID_REQUEST", "Yêu cầu không hợp lệ"),
    UNAUTHENTICATED(401, "UNAUTHENTICATED", "Chưa xác thực người dùng"),
    ACCESS_DENIED(403, "ACCESS_DENIED", "Không có quyền truy cập"),
    PROVIDER_NOT_FOUND(404, "PROVIDER_NOT_FOUND", "Không tìm thấy nhà vận chuyển"),
    PROVIDER_CONFIG_NOT_FOUND(404, "PROVIDER_CONFIG_NOT_FOUND", "Không tìm thấy cấu hình nhà vận chuyển"),
    PROVIDER_SERVICE_NOT_FOUND(404, "PROVIDER_SERVICE_NOT_FOUND", "Không tìm thấy gói dịch vụ vận chuyển"),
    RATE_NOT_FOUND(404, "RATE_NOT_FOUND", "Không tìm thấy bảng giá vận chuyển phù hợp"),
    SHIPMENT_NOT_FOUND(404, "SHIPMENT_NOT_FOUND", "Không tìm thấy đơn vận chuyển"),
    SHIPMENT_CANNOT_CANCEL(400, "SHIPMENT_CANNOT_CANCEL", "Không thể hủy đơn vận chuyển ở trạng thái hiện tại"),
    TRACKING_NOT_FOUND(404, "TRACKING_NOT_FOUND", "Không tìm thấy thông tin tracking"),
    WEBHOOK_VERIFICATION_FAILED(400, "WEBHOOK_VERIFICATION_FAILED", "Xác thực chữ ký Webhook thất bại"),
    CARRIER_INTEGRATION_ERROR(502, "CARRIER_INTEGRATION_ERROR", "Lỗi kết nối API đối tác vận chuyển"),
    CARRIER_API_ERROR(502, "CARRIER_API_ERROR", "Lỗi gọi API nhà vận chuyển"),
    INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "Lỗi hệ thống nội bộ");

    private final int statusCode;
    private final String name;
    private final String message;

    ErrorCode(int statusCode, String name, String message) {
        this.statusCode = statusCode;
        this.name = name;
        this.message = message;
    }
}
