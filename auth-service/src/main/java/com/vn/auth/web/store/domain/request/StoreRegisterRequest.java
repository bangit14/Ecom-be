package com.vn.auth.web.store.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreRegisterRequest {
    // Store information
    private String storeName;
    private String taxNumber;
    private String city;
    private String description;
    private String logo;
    private String banner;

    // Owner Account information
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String phone;
}
