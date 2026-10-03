package com.vn.report.core.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, Object>> handleAppException(AppException ex) {
        ErrorCode code = ex.getErrorCode();
        return ResponseEntity.status(code.getStatusCode())
                .body(Map.of(
                        "code", code.getName(),
                        "message", ex.getMessage() != null ? ex.getMessage() : code.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(500)
                .body(Map.of(
                        "code", "INTERNAL_SERVER_ERROR",
                        "message", ex.getMessage() != null ? ex.getMessage() : "Unknown error"
                ));
    }
}
