package com.vn.auth.web.user.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String role;
    private String type;
    private UUID storeId;
    private boolean isActive;
    private LocalDateTime createdAt;
    private List<String> roles;
}
