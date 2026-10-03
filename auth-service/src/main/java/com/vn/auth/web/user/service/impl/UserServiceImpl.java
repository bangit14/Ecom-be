package com.vn.auth.web.user.service.impl;

import com.vn.auth.core.exception.AppException;
import com.vn.auth.core.exception.ErrorCode;
import com.vn.auth.core.security.CustomUserDetails;
import com.vn.auth.core.security.SecurityUtils;
import com.vn.auth.core.service.EmailService;
import com.vn.auth.web.account.domain.entity.Account;
import com.vn.auth.web.account.domain.request.ChangePasswordRequest;
import com.vn.auth.web.account.domain.request.ResetPasswordRequest;
import com.vn.auth.web.account.repository.AccountRepository;
import com.vn.auth.web.user.domain.entity.UserProfile;
import com.vn.auth.web.user.domain.request.ProfileUpdateRequest;
import com.vn.auth.web.user.domain.response.PageResponse;
import com.vn.auth.web.user.domain.response.UserDetailResponse;
import com.vn.auth.web.user.domain.response.UserResponse;
import com.vn.auth.web.user.domain.response.UserSummaryResponse;
import com.vn.auth.web.user.repository.UserProfileRepository;
import com.vn.auth.web.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "User-Service")
public class UserServiceImpl implements IUserService {

    private final AccountRepository accountRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(Pageable pageable) {
        Page<UserResponse> page = accountRepository.findAll(pageable).map(this::mapToUserResponse);
        return PageResponse.<UserResponse>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .empty(page.isEmpty())
                .build();
    }

    @Override
    public UserDetailResponse getCurrentUser() {
        CustomUserDetails userDetails = SecurityUtils.getCurrentUser();
        return getUserDetail(userDetails.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return mapToUserDetailResponse(account);
    }

    @Override
    @Transactional
    public void updateProfile(UUID id, ProfileUpdateRequest request) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseGet(() -> {
                    Account account = accountRepository.findById(id)
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                    return UserProfile.builder().account(account).build();
                });

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            profile.setName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone().trim());
        }

        userProfileRepository.save(profile);
        log.info("Updated profile for user: {}", id);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String passwordNew = "Ecommerce@14";
        account.setPasswordHash(passwordEncoder.encode(passwordNew));
        accountRepository.save(account);

        String name = account.getProfile() != null ? account.getProfile().getName() : account.getUsername();
        try {
            emailService.sendPasswordResetEmail(account.getEmail(), name, passwordNew);
        } catch (Exception e) {
            log.warn("Could not send password reset email: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void changePassword(UUID id, ChangePasswordRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), account.getPasswordHash())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_CORRECT);
        }

        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
        log.info("Password changed successfully for account: {}", account.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse getUserSummary(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String name = account.getProfile() != null ? account.getProfile().getName() : account.getUsername();
        String phone = account.getProfile() != null ? account.getProfile().getPhone() : null;

        return UserSummaryResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .email(account.getEmail())
                .fullName(name)
                .phone(phone)
                .role(account.getRole())
                .type(account.getType())
                .storeId(account.getStoreId())
                .active(true)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse getUserSummary(String idStr) {
        try {
            UUID id = UUID.fromString(idStr);
            return getUserSummary(id);
        } catch (Exception e) {
            // Fallback try find by username or email
            Account account = accountRepository.findByUsernameOrEmail(idStr)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            return getUserSummary(account.getId());
        }
    }

    @Override
    public long countUsers() {
        return accountRepository.count();
    }

    private UserResponse mapToUserResponse(Account account) {
        String name = account.getProfile() != null ? account.getProfile().getName() : account.getUsername();
        String phone = account.getProfile() != null ? account.getProfile().getPhone() : null;

        return UserResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .email(account.getEmail())
                .fullName(name)
                .phone(phone)
                .role(account.getRole())
                .type(account.getType())
                .storeId(account.getStoreId())
                .build();
    }

    private UserDetailResponse mapToUserDetailResponse(Account account) {
        String name = account.getProfile() != null ? account.getProfile().getName() : account.getUsername();
        String phone = account.getProfile() != null ? account.getProfile().getPhone() : null;

        return UserDetailResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .email(account.getEmail())
                .fullName(name)
                .phone(phone)
                .role(account.getRole())
                .type(account.getType())
                .storeId(account.getStoreId())
                .isActive(true)
                .createdAt(account.getCreatedAt())
                .roles(List.of(account.getRole()))
                .build();
    }
}
