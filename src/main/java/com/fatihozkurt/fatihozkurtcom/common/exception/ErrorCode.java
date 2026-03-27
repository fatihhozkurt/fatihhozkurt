package com.fatihozkurt.fatihozkurtcom.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Enumerates stable backend error codes.
 */
public enum ErrorCode {
    USR001(HttpStatus.NOT_FOUND, "error.USR001"),
    USR002(HttpStatus.CONFLICT, "error.USR002"),
    AUTH001(HttpStatus.UNAUTHORIZED, "error.AUTH001"),
    AUTH002(HttpStatus.UNAUTHORIZED, "error.AUTH002"),
    AUTH003(HttpStatus.UNAUTHORIZED, "error.AUTH003"),
    AUTH004(HttpStatus.BAD_REQUEST, "error.AUTH004"),
    AUTH005(HttpStatus.BAD_REQUEST, "error.AUTH005"),
    VAL001(HttpStatus.BAD_REQUEST, "error.VAL001"),
    SEC001(HttpStatus.TOO_MANY_REQUESTS, "error.SEC001"),
    SEC002(HttpStatus.FORBIDDEN, "error.SEC002"),
    SEC003(HttpStatus.FORBIDDEN, "error.SEC003"),
    SYS002(HttpStatus.INTERNAL_SERVER_ERROR, "error.SYS002"),
    SYS001(HttpStatus.INTERNAL_SERVER_ERROR, "error.SYS001");

    private final HttpStatus status;
    private final String messageKey;

    ErrorCode(HttpStatus status, String messageKey) {
        this.status = status;
        this.messageKey = messageKey;
    }

    /**
     * Returns mapped HTTP status.
     *
     * @return status
     */
    public HttpStatus status() {
        return status;
    }

    /**
     * Returns i18n message key.
     *
     * @return key
     */
    public String messageKey() {
        return messageKey;
    }
}
