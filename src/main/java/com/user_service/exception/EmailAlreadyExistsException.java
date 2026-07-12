package com.user_service.exception;

public class EmailAlreadyExistsException extends UserServiceException {

    private static final String ERROR_CODE = "001";

    public EmailAlreadyExistsException(String email) {
        super(
                ERROR_CODE,
                String.format("Пользователь с email %s уже существует ", email)
        );
    }
}
