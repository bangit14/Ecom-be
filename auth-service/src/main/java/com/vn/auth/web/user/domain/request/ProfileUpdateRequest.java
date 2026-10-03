package com.vn.auth.web.user.domain.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ProfileUpdateRequest {
    private String fullName;
    private String phone;
    private LocalDate dateOfBirth;
}
