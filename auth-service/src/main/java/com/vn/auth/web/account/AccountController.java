package com.vn.auth.web.account;

import com.vn.auth.core.security.SecurityUtils;
import com.vn.auth.web.account.domain.request.ChangePasswordRequest;
import com.vn.auth.web.account.domain.request.LoginRequest;
import com.vn.auth.web.account.domain.response.AuthResponse;
import com.vn.auth.web.account.domain.response.TokenResponse;
import com.vn.auth.web.account.service.IAuthService;
import com.vn.auth.web.user.domain.response.UserDetailResponse;
import com.vn.auth.web.user.service.IUserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/account", "/api/account"})
@RequiredArgsConstructor
@Slf4j(topic = "Account-Controller")
public class AccountController {

    private final IAuthService authService;
    private final IUserService userService;

    /**
     * Orderno-style Authentication endpoint for Interceptors / API Gateway
     */
    @PostMapping("/authentication")
    public ResponseEntity<AuthResponse> authentication(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(authService.authenticate(token));
    }

    /**
     * Orderno-style Login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(loginRequest, response));
    }

    /**
     * Get account detail by id
     */
    @GetMapping("/getAccountById")
    public ResponseEntity<UserDetailResponse> getAccountById(@RequestParam("id") UUID id) {
        return ResponseEntity.ok(userService.getUserDetail(id));
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        userService.changePassword(currentUserId, changePasswordRequest);
        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
    }
}
