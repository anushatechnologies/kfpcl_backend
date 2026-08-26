package com.kfpcl.user.entity;

public enum Role {
    BUYER,
    SELLER,
    ADMIN;

    public static Role fromString(String value) {
        if (value == null) return null;
        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase(value.trim())) {
                return role;
            }
        }
        return null;
    }
}
