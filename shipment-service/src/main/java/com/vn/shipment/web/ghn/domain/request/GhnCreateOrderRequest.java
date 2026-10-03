package com.vn.shipment.web.ghn.domain.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vn.shipment.web.ghn.domain.dto.GhnOrderItemDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GhnCreateOrderRequest {

    // Bên trả phí: 1: Shop, 2: Người nhận
    @JsonProperty("payment_type_id")
    @NotNull(message = "payment_type_id không được để trống (1: Shop, 2: Người nhận)")
    @Builder.Default
    private Integer paymentTypeId = 1;

    // Ghi chú cho tài xế
    @JsonProperty("note")
    private String note;

    // Ghi chú bắt buộc: CHOTHUHANG, CHOXEMHANGKHONGTHU, KHONGCHOXEMHANG
    @JsonProperty("required_note")
    @NotBlank(message = "required_note không được để trống (CHOTHUHANG, CHOXEMHANGKHONGTHU, KHONGCHOXEMHANG)")
    @Builder.Default
    private String requiredNote = "CHOXEMHANGKHONGTHU";

    // Mã đơn hàng nội bộ của shop (chống trùng đơn)
    @JsonProperty("client_order_code")
    private String clientOrderCode;

    // --- Thông tin người gửi (Nếu để trống GHN lấy theo ShopId) ---
    @JsonProperty("from_name")
    private String fromName;

    @JsonProperty("from_phone")
    private String fromPhone;

    @JsonProperty("from_hotline")
    private String fromHotline;

    @JsonProperty("from_address")
    private String fromAddress;

    @JsonProperty("from_ward_name")
    private String fromWardName;

    @JsonProperty("from_district_name")
    private String fromDistrictName;

    @JsonProperty("from_province_name")
    private String fromProvinceName;

    @JsonProperty("from_district_id")
    private Integer fromDistrictId;

    @JsonProperty("from_ward_code")
    private String fromWardCode;

    @JsonProperty("is_new_from_address")
    @Builder.Default
    private Boolean isNewFromAddress = false;

    // --- Thông tin trả hàng (Nếu giao thất bại) ---
    @JsonProperty("return_name")
    private String returnName;

    @JsonProperty("return_phone")
    private String returnPhone;

    @JsonProperty("return_address")
    private String returnAddress;

    @JsonProperty("return_ward_name")
    private String returnWardName;

    @JsonProperty("return_district_name")
    private String returnDistrictName;

    @JsonProperty("return_province_name")
    private String returnProvinceName;

    @JsonProperty("return_district_id")
    private Integer returnDistrictId;

    @JsonProperty("return_ward_code")
    private String returnWardCode;

    @JsonProperty("is_new_return_address")
    @Builder.Default
    private Boolean isNewReturnAddress = false;

    // --- Thông tin người nhận ---
    @JsonProperty("to_name")
    @NotBlank(message = "to_name không được để trống")
    private String toName;

    @JsonProperty("to_phone")
    @NotBlank(message = "to_phone không được để trống")
    private String toPhone;

    @JsonProperty("to_address")
    @NotBlank(message = "to_address không được để trống")
    private String toAddress;

    @JsonProperty("to_ward_name")
    private String toWardName;

    @JsonProperty("to_district_name")
    private String toDistrictName;

    @JsonProperty("to_province_name")
    private String toProvinceName;

    @JsonProperty("to_district_id")
    private Integer toDistrictId;

    @JsonProperty("to_ward_code")
    private String toWardCode;

    @JsonProperty("is_new_to_address")
    @Builder.Default
    private Boolean isNewToAddress = false;

    // --- Tiền thu hộ và giá trị hàng hóa ---
    @JsonProperty("cod_amount")
    @Builder.Default
    private Long codAmount = 0L;

    @JsonProperty("cod_failed_amount")
    @Builder.Default
    private Long codFailedAmount = 0L;

    @JsonProperty("insurance_value")
    @Builder.Default
    private Long insuranceValue = 0L;

    @JsonProperty("order_value")
    @Builder.Default
    private Long orderValue = 0L;

    @JsonProperty("coupon")
    private String coupon;

    // --- Dịch vụ và kích thước ---
    // 2: Dưới 20kg, 5: Trên 20kg hoặc nhiều kiện
    @JsonProperty("service_type_id")
    @Builder.Default
    private Integer serviceTypeId = 2;

    @JsonProperty("service_id")
    private Integer serviceId;

    // Mô tả hàng hóa (bắt buộc khi items trống)
    @JsonProperty("content")
    private String content;

    // Trọng lượng gói hàng (gram, max 50000)
    @JsonProperty("weight")
    @NotNull(message = "weight không được để trống (gram)")
    private Integer weight;

    // Kích thước (cm, max 200)
    @JsonProperty("length")
    private Integer length;

    @JsonProperty("width")
    private Integer width;

    @JsonProperty("height")
    private Integer height;

    // Bưu cục shop mang hàng đến gửi (0: GHN đến lấy tại shop)
    @JsonProperty("pick_station_id")
    @Builder.Default
    private Integer pickStationId = 0;

    // Ca lấy hàng
    @JsonProperty("pick_shift")
    private List<Integer> pickShift;

    // Danh sách sản phẩm kiện hàng
    @JsonProperty("items")
    private List<GhnOrderItemDto> items;
}
