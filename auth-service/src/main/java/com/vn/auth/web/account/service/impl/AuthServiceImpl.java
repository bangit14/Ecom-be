package com.vn.auth.web.account.service.impl;

import com.vn.auth.core.enums.AccountTypeEnum;
import com.vn.auth.core.enums.RoleAccountEnum;
import com.vn.auth.core.enums.TokenType;
import com.vn.auth.core.exception.AppException;
import com.vn.auth.core.exception.ErrorCode;
import com.vn.auth.core.security.CustomUserDetails;
import com.vn.auth.core.service.EmailService;
import com.vn.auth.core.service.JwtService;
import com.vn.auth.core.service.RefreshTokenService;
import com.vn.auth.core.service.TokenBlacklistService;
import com.vn.auth.web.account.domain.entity.Account;
import com.vn.auth.web.account.domain.entity.VerificationToken;
import com.vn.auth.web.account.domain.request.GoogleRegisterRequest;
import com.vn.auth.web.account.domain.request.LoginRequest;
import com.vn.auth.web.account.domain.request.RegisterRequest;
import com.vn.auth.web.account.domain.request.VerifyEmailRequest;
import com.vn.auth.web.account.domain.response.AuthResponse;
import com.vn.auth.web.account.domain.response.TokenResponse;
import com.vn.auth.web.account.repository.AccountRepository;
import com.vn.auth.web.account.repository.VerificationTokenRepository;
import com.vn.auth.web.account.service.IAuthService;
import com.vn.auth.web.store.domain.request.StoreRegisterRequest;
import com.vn.auth.web.store.domain.response.StoreResponse;
import com.vn.auth.web.store.service.IStoreService;
import com.vn.auth.web.user.domain.entity.UserProfile;
import com.vn.auth.web.user.repository.UserProfileRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vn.auth.core.rabbitmq.event.RegistrationEvent;
import com.vn.auth.core.rabbitmq.producer.AuthRabbitMQProducer;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "Auth-Service")
public class AuthServiceImpl implements IAuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private final AccountRepository accountRepository;
    private final UserProfileRepository userProfileRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final IStoreService storeService;
    private final AuthRabbitMQProducer authRabbitMQProducer;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Account account = Account.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .type(AccountTypeEnum.DEFAULT.getValue())
                .role(RoleAccountEnum.CUSTOMER.getValue())
                .build();

        account = accountRepository.save(account);

        UserProfile profile = UserProfile.builder()
                .account(account)
                .name(request.getFullName())
                .phone(request.getPhone())
                .build();

        userProfileRepository.save(profile);

        String code = generateVerificationCode();

        VerificationToken vToken = VerificationToken.builder()
                .token(code)
                .accountId(account.getId())
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();

        tokenRepository.save(vToken);

        RegistrationEvent event = RegistrationEvent.builder()
                .userId(account.getId())
                .email(account.getEmail())
                .verificationToken(code)
                .fullName(profile.getName())
                .build();
        authRabbitMQProducer.sendRegistrationVerification(event);
    }

    @Override
    @Transactional
    public TokenResponse registerGoogle(GoogleRegisterRequest request, HttpServletResponse response) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email này đã được đăng ký tài khoản. Vui lòng chuyển sang Đăng nhập bằng Google.");
        }

        // Tạo username từ email nếu chưa có
        String username = request.getEmail().split("@")[0];
        if (accountRepository.existsByUsername(username)) {
            username = username + "_" + UUID.randomUUID().toString().substring(0, 6);
        }

        // Đăng ký bằng Google không cần password
        Account account = Account.builder()
                .username(username)
                .email(request.getEmail())
                .passwordHash(null)
                .type(AccountTypeEnum.GOOGLE.getValue())
                .role(RoleAccountEnum.CUSTOMER.getValue())
                .isVerified(true)
                .build();

        account = accountRepository.save(account);

        UserProfile profile = UserProfile.builder()
                .account(account)
                .name(request.getFullName())
                .phone(request.getPhone())
                .build();

        userProfileRepository.save(profile);
        account.setProfile(profile);

        log.info("Google account registered successfully for email: {}", request.getEmail());
        return generateTokensAndSetCookieAndRedis(buildUserDetails(account), response);
    }

    @Override
    @Transactional
    public TokenResponse loginGoogle(GoogleRegisterRequest request, HttpServletResponse response) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Tài khoản Google này chưa được đăng ký trong hệ thống. Vui lòng chọn Đăng ký bằng Google."));

        if (!AccountTypeEnum.GOOGLE.getValue().equals(account.getType())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Email này đã được đăng ký bằng mật khẩu thông thường. Vui lòng đăng nhập bằng mật khẩu.");
        }

        return generateTokensAndSetCookieAndRedis(buildUserDetails(account), response);
    }

    // ======================== ĐĂNG KÝ STORE ========================

    @Override
    public StoreResponse registerStore(StoreRegisterRequest request) {
        return storeService.registerStore(request);
    }

    // ======================== ĐĂNG NHẬP ========================

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request, HttpServletResponse response) {
        String loginIdentifier = request.getLoginIdentifier();
        if (loginIdentifier == null || request.getPassword() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Tên đăng nhập/email và mật khẩu không được để trống");
        }

        Account account = accountRepository.findByUsernameOrEmail(loginIdentifier)
                .orElseThrow(() -> new AppException(ErrorCode.LOGIN_FAILED, "Tài khoản hoặc mật khẩu không chính xác"));

        // Tài khoản Google không thể login bằng password
        if (AccountTypeEnum.GOOGLE.getValue().equals(account.getType())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Tài khoản này được đăng ký bằng Google. Vui lòng đăng nhập bằng Google.");
        }

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new AppException(ErrorCode.LOGIN_FAILED, "Tài khoản hoặc mật khẩu không chính xác");
        }

        // Kiểm tra xác thực email cho tài khoản DEFAULT
        if (AccountTypeEnum.DEFAULT.getValue().equals(account.getType())) {
            if (!isAccountVerified(account)) {
                // Tự động phát sinh mã OTP mới và bắn RabbitMQ event sang notification-service
                resendVerificationEmail(account.getEmail());

                throw new AppException(
                        ErrorCode.EMAIL_NOT_VERIFIED,
                        "Tài khoản chưa được xác minh email. Mã xác thực mới đã được gửi tới email của bạn.",
                        Map.of("email", account.getEmail())
                );
            }
        }

        return generateTokensAndSetCookieAndRedis(buildUserDetails(account), response);
    }

    // ======================== XÁC THỰC TOKEN ========================

    @Override
    public AuthResponse authenticate(String token) {
        CustomUserDetails userDetails = jwtService.extractUserDetailsFromToken(token);
        if (userDetails == null || userDetails.getId() == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return AuthResponse.builder()
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .role(userDetails.getRole())
                .type(userDetails.getType())
                .storeId(userDetails.getStoreId())
                .name(userDetails.getFullName())
                .token(token)
                .tokenType("Bearer")
                .active(userDetails.isEnabled())
                .build();
    }

    // ======================== REFRESH TOKEN ========================

    @Override
    @Transactional(readOnly = true)
    public TokenResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        try {
            String userIdStr = jwtService.extractSubject(refreshToken, TokenType.REFRESH_TOKEN);
            UUID userId = UUID.fromString(userIdStr);

            if (!refreshTokenService.isValid(userId, refreshToken)) {
                throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
            }

            refreshTokenService.revoke(userId);

            Account account = accountRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            return generateTokensAndSetCookieAndRedis(buildUserDetails(account), response);

        } catch (Exception e) {
            log.error("Refresh token failed: {}", e.getMessage());
            throw new AppException(ErrorCode.REFRESH_TOKEN_FAILED);
        }
    }

    // ======================== ĐĂNG XUẤT ========================

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            try {
                String jti = jwtService.extractJti(accessToken);
                long ttl = jwtService.getRemainingExpirationTime(accessToken);
                tokenBlacklistService.blacklist(jti, ttl);
                log.debug("Access token blacklisted for jti={}", jti);
            } catch (Exception e) {
                log.warn("Could not blacklist access token: {}", e.getMessage());
            }
        }

        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            try {
                String userIdStr = jwtService.extractSubject(refreshToken, TokenType.REFRESH_TOKEN);
                refreshTokenService.revoke(UUID.fromString(userIdStr));
            } catch (Exception e) {
                log.warn("Could not revoke refresh token: {}", e.getMessage());
            }
        }

        ResponseCookie clearCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

        log.info("User logged out successfully");
    }

    // ======================== XÁC THỰC EMAIL ========================

    @Override
    @Transactional
    public TokenResponse verifyEmail(VerifyEmailRequest request, HttpServletResponse response) {
        VerificationToken vToken = tokenRepository.findByToken(request.getCode())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN, "Mã xác thực không chính xác"));

        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy tài khoản tương ứng với email"));

        if (vToken.getExpiryDate().isBefore(LocalDateTime.now()) || vToken.isUsed()) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED, "Mã xác thực đã hết hạn hoặc đã được sử dụng");
        }

        vToken.setUsed(true);
        tokenRepository.save(vToken);

        account.setVerified(true);
        accountRepository.save(account);

        log.info("Email verified successfully for account: {}", account.getEmail());
        return generateTokensAndSetCookieAndRedis(buildUserDetails(account), response);
    }

    private boolean isAccountVerified(Account account) {
        if (AccountTypeEnum.GOOGLE.getValue().equals(account.getType())) {
            return true;
        }
        return account.isVerified() || tokenRepository.existsByAccountIdAndUsedTrue(account.getId());
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        tokenRepository.findByAccountIdAndUsedFalse(account.getId()).ifPresent(t -> {
            t.setUsed(true);
            tokenRepository.save(t);
        });

        String newCode = generateVerificationCode();

        VerificationToken vToken = VerificationToken.builder()
                .token(newCode)
                .accountId(account.getId())
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();

        tokenRepository.save(vToken);

        String name = account.getProfile() != null ? account.getProfile().getName() : account.getUsername();
        RegistrationEvent event = RegistrationEvent.builder()
                .userId(account.getId())
                .email(account.getEmail())
                .verificationToken(newCode)
                .fullName(name)
                .build();
        authRabbitMQProducer.sendRegistrationVerification(event);
    }

    // ======================== PRIVATE HELPERS ========================

    private CustomUserDetails buildUserDetails(Account account) {
        String fullName = account.getProfile() != null ? account.getProfile().getName() : account.getUsername();
        String phone = account.getProfile() != null ? account.getProfile().getPhone() : null;

        return CustomUserDetails.builder()
                .id(account.getId())
                .storeId(account.getStoreId())
                .username(account.getUsername())
                .email(account.getEmail())
                .fullName(fullName)
                .phone(phone)
                .password(account.getPasswordHash())
                .role(account.getRole())
                .type(account.getType())
                .roles(List.of(account.getRole()))
                .active(true)
                .build();
    }

    private String generateVerificationCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1000000));
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> REFRESH_TOKEN_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private TokenResponse generateTokensAndSetCookieAndRedis(CustomUserDetails user, HttpServletResponse response) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        refreshTokenService.saveOrUpdate(user.getId(), refreshToken);

        long cookieAge = refreshExpiration > 10000000L ? refreshExpiration / 1000L : refreshExpiration;

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofSeconds(cookieAge))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        AuthResponse authUser = AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .type(user.getType())
                .storeId(user.getStoreId())
                .name(user.getFullName())
                .token(accessToken)
                .tokenType("Bearer")
                .active(user.isEnabled())
                .build();

        return TokenResponse.builder()
                .access_token(accessToken)
                .refresh_token(refreshToken)
                .token_type("Bearer")
                .user(authUser)
                .build();
    }
}
