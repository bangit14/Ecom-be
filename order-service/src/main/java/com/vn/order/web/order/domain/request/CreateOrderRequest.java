package com.vn.order.web.order.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private List<SelectedCartItem> selectedItems;
    private Long addressId;
    private String note;
    private String couponCode;
    private String paymentMethod;
}
