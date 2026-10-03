package com.vn.auth.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, Object>> handleAppException(AppException ex) {
        log.warn("AppException: {} - {}", ex.getErrorCode().name(), ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("code", ex.getErrorCode().getStatusCode());
        body.put("error", ex.getErrorCode().name());
        body.put("message", ex.getMessage());
        if (ex.getData() != null) {
            body.put("data", ex.getData());
        }
        body.put("timestamp", LocalDateTime.now());

        HttpStatus status = switch (ex.getErrorCode()) {
            case UNAUTHENTICATED, INVALID_TOKEN, TOKEN_EXPIRED, REFRESH_TOKEN_EXPIRED, REFRESH_TOKEN_NOT_FOUND, TOKEN_BLACKLISTED ->
                    HttpStatus.UNAUTHORIZED;
            case ACCESS_DENIED, UNAUTHORIZED, EMAIL_NOT_VERIFIED -> HttpStatus.FORBIDDEN;
            case USER_NOT_FOUND, ROLE_NOT_FOUND, EMAIL_NOT_FOUND, ADDRESS_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case USER_ALREADY_EXISTS, EMAIL_ALREADY_EXISTS, EMAIL_ALREADY_VERIFIED -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("code", ErrorCode.INFORMATION_LOGIN_NOT_CORRECT.getStatusCode());
        body.put("error", ErrorCode.INFORMATION_LOGIN_NOT_CORRECT.name());
        body.put("message", "Tên đăng nhập hoặc mật khẩu không chính xác");
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("code", ErrorCode.ACCESS_DENIED.getStatusCode());
        body.put("error", ErrorCode.ACCESS_DENIED.name());
        body.put("message", "Bạn không có quyền thực hiện thao tác này");
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unhandled exception: ", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("code", 5000);
        body.put("error", "INTERNAL_SERVER_ERROR");
        body.put("message", ex.getMessage());
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
