package com.kfpcl.common.security;

public final class UserContext {

    private static final ThreadLocal<AuthenticatedUser> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setCurrentUser(AuthenticatedUser user) {
        CURRENT_USER.set(user);
    }

    public static AuthenticatedUser getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static String getRequiredUserId() {
        AuthenticatedUser user = CURRENT_USER.get();
        if (user == null || user.getUserId() == null) {
            throw new IllegalStateException("No authenticated user found in current request context");
        }
        return user.getUserId();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
