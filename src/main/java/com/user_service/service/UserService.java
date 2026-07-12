package com.user_service.service;

import com.user_service.dto.UserRequestDto;
import com.user_service.dto.UserResponseDto;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserResponseDto createUser(UserRequestDto userRequestDto);

    Optional<UserResponseDto> findUserById(Long id);

    Optional<UserResponseDto> findUserByEmail(String email);

    List<UserResponseDto> findUsersByName(String name);

    List<UserResponseDto> findAllUsers();

    UserResponseDto updateUser(Long id, UserRequestDto userRequestDto);

    boolean deleteUser(Long id);

    boolean isUniqueEmail(String email);
}
