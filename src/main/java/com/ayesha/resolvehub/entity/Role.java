package com.ayesha.resolvehub.entity;

public enum Role {
    ADMIN,
    AGENT,
    REPORTER;

    public static Role fromString(String value) {
        if (value == null || value.isBlank()) {
            return REPORTER;
        }
        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return REPORTER;
        }
    }
}
