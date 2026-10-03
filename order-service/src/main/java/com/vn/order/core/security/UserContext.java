package com.vn.order.core.security;

public class UserContext {

    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserEmail = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserRoles = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserFullName = new ThreadLocal<>();

    public static Long getUserId() {
        return currentUserId.get();
    }

    public static void setUserId(Long userId) {
        currentUserId.set(userId);
    }

    public static String getUserEmail() {
        return currentUserEmail.get();
    }

    public static void setUserEmail(String email) {
        currentUserEmail.set(email);
    }

    public static String getUserRoles() {
        return currentUserRoles.get();
    }

    public static void setUserRoles(String roles) {
        currentUserRoles.set(roles);
    }

    public static String getFullName() {
        return currentUserFullName.get();
    }

    public static void setFullName(String fullName) {
        currentUserFullName.set(fullName);
    }

    public static void clear() {
        currentUserId.remove();
        currentUserEmail.remove();
        currentUserRoles.remove();
        currentUserFullName.remove();
    }
}
