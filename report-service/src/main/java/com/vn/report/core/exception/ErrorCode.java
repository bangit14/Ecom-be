package com.vn.report.core.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_REQUEST(400, "INVALID_REQUEST", "Invalid request"),
    UNAUTHENTICATED(401, "UNAUTHENTICATED", "Unauthenticated"),
    ACCESS_DENIED(403, "ACCESS_DENIED", "Access denied"),
    INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "Internal server error");

    private final int statusCode;
    private final String name;
    private final String message;

    ErrorCode(int statusCode, String name, String message) {
        this.statusCode = statusCode;
        this.name = name;
        this.message = message;
    }
}
