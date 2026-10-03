package com.vn.auth.web.user.service;

import com.vn.auth.web.account.domain.request.ChangePasswordRequest;
import com.vn.auth.web.account.domain.request.ResetPasswordRequest;
import com.vn.auth.web.user.domain.request.ProfileUpdateRequest;
import com.vn.auth.web.user.domain.response.PageResponse;
import com.vn.auth.web.user.domain.response.UserDetailResponse;
import com.vn.auth.web.user.domain.response.UserResponse;
import com.vn.auth.web.user.domain.response.UserSummaryResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IUserService {

    PageResponse<UserResponse> getUsers(Pageable pageable);

    UserDetailResponse getCurrentUser();

    UserDetailResponse getUserDetail(UUID id);

    void updateProfile(UUID id, ProfileUpdateRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(UUID id, ChangePasswordRequest request);

    UserSummaryResponse getUserSummary(UUID id);

    UserSummaryResponse getUserSummary(String id);

    long countUsers();
}
