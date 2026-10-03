package com.vn.auth.web.account.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private UUID id;
    private String username;
    private String email;
    private String role;
    private String type;
    private UUID storeId;
    private String name;
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    @Builder.Default
    private boolean active = true;
}
