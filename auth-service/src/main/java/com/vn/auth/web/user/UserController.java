package com.vn.auth.web.user;

import com.vn.auth.core.enums.RoleAccountEnum;
import com.vn.auth.core.security.Privileges;
import com.vn.auth.core.security.SecurityUtils;
import com.vn.auth.core.util.PageableUtils;
import com.vn.auth.web.account.domain.request.ChangePasswordRequest;
import com.vn.auth.web.account.domain.request.ResetPasswordRequest;
import com.vn.auth.web.user.domain.request.ProfileUpdateRequest;
import com.vn.auth.web.user.domain.response.PageResponse;
import com.vn.auth.web.user.domain.response.UserDetailResponse;
import com.vn.auth.web.user.domain.response.UserResponse;
import com.vn.auth.web.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j(topic = "User-Controller")
public class UserController {

    private final IUserService userService;

    @GetMapping({"", "/list"})
    @Privileges({RoleAccountEnum.ADMIN})
    public ResponseEntity<PageResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {

        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, sortDir);
        PageResponse<UserResponse> result = userService.getUsers(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailResponse> getUserDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserDetail(id));
    }

    @GetMapping({"/me", "/me/profile"})
    public ResponseEntity<UserDetailResponse> getCurrentUserProfile() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PutMapping({"/profile", "/update-profile/{id}"})
    public ResponseEntity<Void> updateProfile(
            @PathVariable(required = false) UUID id,
            @RequestBody ProfileUpdateRequest request) {
        UUID targetId = id != null ? id : SecurityUtils.getCurrentUserId();
        userService.updateProfile(targetId, request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.accepted().build();
    }

    @PutMapping({"/change-password", "/change-password/{id}"})
    public ResponseEntity<Void> changePassword(
            @PathVariable(required = false) UUID id,
            @RequestBody ChangePasswordRequest request) {
        UUID targetId = id != null ? id : SecurityUtils.getCurrentUserId();
        userService.changePassword(targetId, request);
        return ResponseEntity.accepted().build();
    }
}
