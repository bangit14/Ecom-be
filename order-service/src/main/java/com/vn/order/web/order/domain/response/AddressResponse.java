package com.vn.order.web.order.domain.response;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String addressLine;
    private String ward;
    private String district;
    private String province;
    private String country;
    private boolean isDefault;
    private boolean isBilling;
    private boolean isShipping;
}
