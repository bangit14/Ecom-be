package com.vn.auth.web.account.domain.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoogleRegisterRequest {
    private String googleToken;
    private String email;
    private String fullName;
    private String phone;
}
