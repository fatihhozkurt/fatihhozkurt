package com.fatihozkurt.fatihozkurtcom.common.exception;

/**
 * Represents a business or security exception with a stable error code.
 */
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] args;

    /**
     * Creates exception with code and optional format arguments.
     *
     * @param errorCode code
     * @param args optional message arguments
     */
    public AppException(ErrorCode errorCode, Object... args) {
        super(errorCode.name());
        this.errorCode = errorCode;
        this.args = args == null ? new Object[0] : args;
    }

    /**
     * Returns error code.
     *
     * @return code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Returns message arguments.
     *
     * @return arguments
     */
    public Object[] getArgs() {
        return args;
    }
}
