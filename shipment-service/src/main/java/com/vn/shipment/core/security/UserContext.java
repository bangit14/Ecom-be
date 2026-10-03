package com.vn.shipment.core.security;

public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_EMAIL = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ROLES = new ThreadLocal<>();
    private static final ThreadLocal<String> FULL_NAME = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setUserEmail(String email) {
        USER_EMAIL.set(email);
    }

    public static String getUserEmail() {
        return USER_EMAIL.get();
    }

    public static void setUserRoles(String roles) {
        USER_ROLES.set(roles);
    }

    public static String getUserRoles() {
        return USER_ROLES.get();
    }

    public static void setFullName(String fullName) {
        FULL_NAME.set(fullName);
    }

    public static String getFullName() {
        return FULL_NAME.get();
    }

    public static boolean hasRole(String role) {
        String roles = USER_ROLES.get();
        if (roles == null) return false;
        return roles.contains(role);
    }

    public static void clear() {
        USER_ID.remove();
        USER_EMAIL.remove();
        USER_ROLES.remove();
        FULL_NAME.remove();
    }
}
