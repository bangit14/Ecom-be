package com.vn.auth.core.enums;

public enum AccountTypeEnum {
    DEFAULT("DEFAULT"),
    GOOGLE("GOOGLE");

    private final String value;

    AccountTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AccountTypeEnum fromValue(String value) {
        for (AccountTypeEnum type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return DEFAULT;
    }
}
