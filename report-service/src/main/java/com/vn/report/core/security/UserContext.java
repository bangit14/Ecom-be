package com.vn.report.core.security;

public class UserContext {
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_FULLNAME_HOLDER = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static void setFullName(String fullName) {
        USER_FULLNAME_HOLDER.set(fullName);
    }

    public static String getFullName() {
        return USER_FULLNAME_HOLDER.get();
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
        USER_FULLNAME_HOLDER.remove();
    }
}
