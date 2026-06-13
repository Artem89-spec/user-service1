package com.user_service.dao;

import com.user_service.entity.User;
import jakarta.persistence.Id;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    List<User> findByName(String name);

    List<User> findAll();

    User update(User user);

    boolean delete(Long id);
}
