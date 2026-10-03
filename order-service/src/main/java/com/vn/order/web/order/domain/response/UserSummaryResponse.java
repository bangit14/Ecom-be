package com.vn.order.web.order.domain.response;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private boolean active;
}
