package com.user_service.exception;

public class InvalidUserDataException extends UserServiceException {

    private static final String ERROR_CODE = "003";
    private final String field;

    public InvalidUserDataException(String field, String message) {
        super(
                ERROR_CODE,
                String.format("Некорректные данные в поле %s: %s", field, message)
        );
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
