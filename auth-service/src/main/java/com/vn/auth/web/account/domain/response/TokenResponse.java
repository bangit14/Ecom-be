package com.vn.auth.web.account.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private String access_token;
    private String refresh_token;
    @Builder.Default
    private String token_type = "Bearer";
    private AuthResponse user;
}
