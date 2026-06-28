package com.user_service.controller;

import com.user_service.exception.EmailAlreadyExistsException;
import com.user_service.exception.InvalidUserDataException;
import com.user_service.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class UserServiceExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(EmailAlreadyExistsException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("errorCode", exception.getErrorCode());
        response.put("message", exception.getUserMessage());
        response.put("timeStamp", LocalDateTime.now());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("errorCode", exception.getErrorCode());
        response.put("message", exception.getUserMessage());
        response.put("timeStamp", LocalDateTime.now());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidUserData(InvalidUserDataException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("errorCode", exception.getErrorCode());
        response.put("message", exception.getUserMessage());
        response.put("timeStamp", LocalDateTime.now());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException exception) {
        Map<String, String> response = new HashMap<>();
        response.put("error", exception.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException exception) {
        Map<String, String> response = new HashMap<>();
        response.put("error", exception.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
}
