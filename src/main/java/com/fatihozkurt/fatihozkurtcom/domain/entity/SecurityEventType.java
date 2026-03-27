package com.fatihozkurt.fatihozkurtcom.domain.entity;

/**
 * Defines security event categories.
 */
public enum SecurityEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    RESET_REQUESTED,
    RESET_COMPLETED,
    RATE_LIMIT_TRIGGERED,
    LOGOUT,
    TOKEN_REFRESHED
}
