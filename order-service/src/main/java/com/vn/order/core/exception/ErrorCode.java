package com.vn.order.core.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_REQUEST(400, "INVALID_REQUEST", "Invalid request"),
    UNAUTHENTICATED(401, "UNAUTHENTICATED", "Unauthenticated"),
    ACCESS_DENIED(403, "ACCESS_DENIED", "Access denied"),
    ORDER_NOT_FOUND(404, "ORDER_NOT_FOUND", "Order not found"),
    ORDER_CANNOT_CANCEL(400, "ORDER_CANNOT_CANCEL", "Cannot cancel order in current state"),
    CART_EMPTY(400, "CART_EMPTY", "Cart is empty"),
    INSUFFICIENT_STOCK(400, "INSUFFICIENT_STOCK", "Insufficient stock for one or more items"),
    PRODUCT_VARIANT_NOT_FOUND(404, "PRODUCT_VARIANT_NOT_FOUND", "Product variant not found"),
    ADDRESS_NOT_FOUND(404, "ADDRESS_NOT_FOUND", "Shipping address not found"),
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
