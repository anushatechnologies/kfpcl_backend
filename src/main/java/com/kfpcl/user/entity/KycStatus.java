package com.kfpcl.user.entity;

public enum KycStatus {
    PENDING,
    SUBMITTED,
    APPROVED,
    REJECTED,
    NOT_APPLICABLE;

    public static KycStatus fromString(String value) {
        if (value == null) return null;
        for (KycStatus status : KycStatus.values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        return null;
    }
}
