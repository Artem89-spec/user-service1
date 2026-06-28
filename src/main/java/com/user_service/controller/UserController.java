package com.user_service.controller;

import com.user_service.dto.UserRequestDto;
import com.user_service.dto.UserResponseDto;
import com.user_service.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        logger.info("POST /api/users - создание      нового пользователя");
        UserResponseDto createdUser = userService.createUser(userRequestDto);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        logger.info("GET /api/users/{} - поиск пользователя по id", id);
        Optional<UserResponseDto> user = userService.findUserById(id);
        return user
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email")
    public ResponseEntity<UserResponseDto> getUserByEmail(@RequestParam String email) {
        logger.info("GET /api/users/email - поиск пользователя по email {}", email);
        Optional<UserResponseDto> user = userService.findUserByEmail(email);
        return user
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name")
    public ResponseEntity<List<UserResponseDto>> getUsersByName(@RequestParam String name) {
        logger.info("GET /api/users/name - поиск пользователя по имени {}", name);
        List<UserResponseDto> users = userService.findUsersByName(name);
        return ResponseEntity.ok(users);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        logger.info("GET /api/users - получение всех пользователей");
        List<UserResponseDto> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDto userRequestDto) {
        logger.info("PUT /api/users/{} - обновление пользователя", id);
        UserResponseDto updatedUser = userService.updateUser(id, userRequestDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("DELETE /api/users/{} - удаление пользователя", id);
        boolean deleted = userService.deleteUser(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
