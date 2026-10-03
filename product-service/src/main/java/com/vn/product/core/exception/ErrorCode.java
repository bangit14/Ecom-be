package com.vn.product.core.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_REQUEST(400, "INVALID_REQUEST", "Invalid request"),
    UNAUTHENTICATED(401, "UNAUTHENTICATED", "Unauthenticated"),
    ACCESS_DENIED(403, "ACCESS_DENIED", "Access denied"),
    PRODUCT_NOT_FOUND(404, "PRODUCT_NOT_FOUND", "Product not found"),
    CATEGORY_NOT_FOUND(404, "CATEGORY_NOT_FOUND", "Category not found"),
    PRODUCT_VARIANT_NOT_FOUND(404, "PRODUCT_VARIANT_NOT_FOUND", "Product variant not found"),
    PRODUCT_IMAGE_NOT_FOUND(404, "PRODUCT_IMAGE_NOT_FOUND", "Product image not found"),
    REVIEW_NOT_FOUND(404, "REVIEW_NOT_FOUND", "Review not found"),
    CATEGORY_ALREADY_EXISTS(409, "CATEGORY_ALREADY_EXISTS", "Category already exists"),
    SLUG_ALREADY_EXISTS(409, "SLUG_ALREADY_EXISTS", "Slug already exists"),
    CATEGORY_HAS_CHILDREN(400, "CATEGORY_HAS_CHILDREN", "Category has children"),
    CANNOT_DELETE_VARIANT_WITH_STOCK(400, "CANNOT_DELETE_VARIANT_WITH_STOCK", "Cannot delete variant with stock"),
    CANNOT_UPLOAD_IMAGE(500, "CANNOT_UPLOAD_IMAGE", "Cannot upload image"),
    IMAGE_FILE_IS_EMPTY(400, "IMAGE_FILE_IS_EMPTY", "Image file is empty"),
    CATEGORY_CIRCULAR_REFERENCE(400, "CATEGORY_CIRCULAR_REFERENCE", "Category circular reference"),
    INSUFFICIENT_STOCK(400, "INSUFFICIENT_STOCK", "Insufficient stock"),
    REVIEW_ALREADY_EXISTS(409, "REVIEW_ALREADY_EXISTS", "You have already reviewed this product");

    private final int statusCode;
    private final String name;
    private final String message;

    ErrorCode(int statusCode, String name, String message) {
        this.statusCode = statusCode;
        this.name = name;
        this.message = message;
    }
}
