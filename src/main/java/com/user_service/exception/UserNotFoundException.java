package com.user_service.exception;

public class UserNotFoundException extends UserServiceException {

    private static final String ERROR_CODE = "002";

    public UserNotFoundException(Long id) {
        super(
                ERROR_CODE,
                String.format("Пользователь с ID %d не найден", id)
        );
    }

    public UserNotFoundException(String email) {
        super(
                ERROR_CODE,
                String.format("Пользователь с email %s не найден", email)
        );
    }

    public UserNotFoundException(Long id, String email) {
        super(
                ERROR_CODE,
                String.format("Пользователь с ID %d и email %s не найден", id, email)
        );
    }
}