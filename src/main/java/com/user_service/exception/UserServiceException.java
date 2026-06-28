package com.user_service.exception;

public abstract class UserServiceException extends RuntimeException {

    private final String errorCode;
    private final String userMessage;

    protected UserServiceException(String errorCode, String userMessage) {
        super(userMessage);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
