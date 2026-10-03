package com.vn.auth.web.account;

import com.vn.auth.web.account.domain.request.GoogleRegisterRequest;
import com.vn.auth.web.account.domain.request.LoginRequest;
import com.vn.auth.web.account.domain.request.RegisterRequest;
import com.vn.auth.web.account.domain.request.ResetPasswordRequest;
import com.vn.auth.web.account.domain.request.VerifyEmailRequest;
import com.vn.auth.web.account.domain.response.AuthResponse;
import com.vn.auth.web.account.domain.response.TokenResponse;
import com.vn.auth.web.account.service.IAuthService;
import com.vn.auth.web.user.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;
    private final IUserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(Map.of("message", "Đăng ký thành công. Vui lòng kiểm tra email để xác thực."));
    }

    @PostMapping("/register/google")
    public ResponseEntity<TokenResponse> registerGoogle(@RequestBody GoogleRegisterRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.registerGoogle(request, response));
    }

    @PostMapping("/login/google")
    public ResponseEntity<TokenResponse> loginGoogle(@RequestBody GoogleRegisterRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.loginGoogle(request, response));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        authService.resendVerificationEmail(email);
        return ResponseEntity.ok(Map.of("message", "Đã gửi lại email xác thực."));
    }

    @PostMapping("/resend-code")
    public ResponseEntity<?> resendCode(@RequestBody Map<String, String> body) {
        return resendVerification(body);
    }

    @PostMapping("/verify-code")
    public ResponseEntity<TokenResponse> verifyCode(@RequestBody VerifyEmailRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.verifyEmail(request, response));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(authService.authenticate(token));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.refresh(request, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Mật khẩu mới đã được gửi tới email của bạn."));
    }
}
