package com.vn.auth.web.account.service;

import com.vn.auth.web.account.domain.request.GoogleRegisterRequest;
import com.vn.auth.web.account.domain.request.LoginRequest;
import com.vn.auth.web.account.domain.request.RegisterRequest;
import com.vn.auth.web.account.domain.request.VerifyEmailRequest;
import com.vn.auth.web.account.domain.response.AuthResponse;
import com.vn.auth.web.account.domain.response.TokenResponse;
import com.vn.auth.web.store.domain.request.StoreRegisterRequest;
import com.vn.auth.web.store.domain.response.StoreResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface IAuthService {
    void register(RegisterRequest request);

    TokenResponse registerGoogle(GoogleRegisterRequest request, HttpServletResponse response);

    TokenResponse loginGoogle(GoogleRegisterRequest request, HttpServletResponse response);

    StoreResponse registerStore(StoreRegisterRequest request);

    TokenResponse login(LoginRequest request, HttpServletResponse response);

    AuthResponse authenticate(String token);

    TokenResponse refresh(HttpServletRequest request, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);

    TokenResponse verifyEmail(VerifyEmailRequest request, HttpServletResponse response);

    void resendVerificationEmail(String email);
}
