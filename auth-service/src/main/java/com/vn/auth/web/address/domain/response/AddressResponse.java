package com.vn.auth.web.address.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private UUID id;
    private UUID userId;
    private String recipentName;
    private String fullName;
    private String phone;
    private String addressDetail;
    private String addressLine;
    private String ward;
    private String district;
    private String province;
    private boolean isDefault;
}
