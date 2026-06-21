package com.user_service.service;

import com.user_service.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserEntity createUser(String name, String email, String age);

    Optional<UserEntity> findUserById(Long id);

    Optional<UserEntity> findUserByEmail(String email);

    List<UserEntity> findUsersByName(String name);

    List<UserEntity> findAllUsers();

    UserEntity updateUser(UserEntity user);

    boolean deleteUser(Long id);

    boolean isUniqueEmail(String email);
}
