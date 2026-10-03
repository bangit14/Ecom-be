package com.vn.auth.web.address.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressCreateRequest {
    private String recipentName;
    private String fullName;
    private String phone;
    private String addressDetail;
    private String addressLine;
    private String ward;
    private String district;
    private String province;
    private boolean isDefault;

    public String resolveRecipentName() {
        if (recipentName != null && !recipentName.trim().isEmpty()) {
            return recipentName.trim();
        }
        return fullName != null ? fullName.trim() : "";
    }

    public String resolveAddressDetail() {
        if (addressDetail != null && !addressDetail.trim().isEmpty()) {
            return addressDetail.trim();
        }
        return addressLine != null ? addressLine.trim() : "";
    }
}
