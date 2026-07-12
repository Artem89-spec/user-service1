package com.user_service.dto;

public class UserNotificationDto {

    private String email;
    private String operation;
    private Long userId;
    private String userName;

    public UserNotificationDto() {
    }

    public UserNotificationDto(String email, String operation, Long userId, String userName) {
        this.email = email;
        this.operation = operation;
        this.userId = userId;
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
