package com.user_service.dao;

import com.user_service.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    UserEntity save(UserEntity user);

    Optional<UserEntity> findById(Long id);

    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findByName(String name);

    List<UserEntity> findAll();

    UserEntity update(UserEntity user);

    boolean delete(Long id);
}
