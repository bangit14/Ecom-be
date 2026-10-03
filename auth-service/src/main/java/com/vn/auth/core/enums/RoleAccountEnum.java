package com.vn.auth.core.enums;

public enum RoleAccountEnum {
    ADMIN("ADMIN"),
    STORE_OWNER("STORE_OWNER"),
    STORE_STAFF("STORE_STAFF"),
    CUSTOMER("CUSTOMER");

    private final String value;

    RoleAccountEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RoleAccountEnum fromValue(String value) {
        for (RoleAccountEnum role : values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        return CUSTOMER;
    }
}
