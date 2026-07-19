package com.user_service.controller;

import com.user_service.dto.UserRequestDto;
import com.user_service.dto.UserResponseDto;
import com.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "User Controller", description = "Управление пользователями")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final String ALL_USERS_REL = "all-users";
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Создание нового пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные при создании пользователя"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует")
    })
    @PostMapping
    public ResponseEntity<EntityModel<UserResponseDto>> createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        logger.info("POST /api/users - создание нового пользователя");
        UserResponseDto createdUser = userService.createUser(userRequestDto);

        EntityModel<UserResponseDto> model = EntityModel.of(createdUser,
                linkTo(methodOn(UserController.class).getUserById(createdUser.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel(ALL_USERS_REL));
        return ResponseEntity.created(
                linkTo(methodOn(UserController.class).getUserById(createdUser.getId())).toUri()).body(model);
    }

    @Operation(summary = "Найти пользователя по его id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponseDto>> getUserById(@PathVariable Long id) {
        logger.info("GET /api/users/{} - поиск пользователя по id", id);
        UserResponseDto foundUser = userService.findUserById(id);

        EntityModel<UserResponseDto> model = EntityModel.of(foundUser,
                linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel(ALL_USERS_REL));
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Найти пользователя по его email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно найден"),
            @ApiResponse(responseCode = "400", description = "Некорректный email"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/email")
    public ResponseEntity<EntityModel<UserResponseDto>> getUserByEmail(@RequestParam String email) {
        logger.info("GET /api/users/email - поиск пользователя по email {}", email);
        UserResponseDto foundUser = userService.findUserByEmail(email);

        EntityModel<UserResponseDto> model = EntityModel.of(foundUser,
                linkTo(methodOn(UserController.class).getUserById(foundUser.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel(ALL_USERS_REL));
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Найти пользователей по имени")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список найденных по имени пользователей")})
    @GetMapping("/name")
    public ResponseEntity<CollectionModel<EntityModel<UserResponseDto>>> getUsersByName(@RequestParam String name) {
        logger.info("GET /api/users/name - поиск пользователя по имени {}", name);
        List<UserResponseDto> users = userService.findUsersByName(name);

        List<EntityModel<UserResponseDto>> models = users.stream()
                .map(userResponseDto -> EntityModel.of(userResponseDto,
                        linkTo(methodOn(UserController.class).getUserById(userResponseDto.getId())).withSelfRel()))
                .toList();
        CollectionModel<EntityModel<UserResponseDto>> collectionModel = CollectionModel.of(models,
                linkTo(methodOn(UserController.class).getAllUsers()).withRel(ALL_USERS_REL));
        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Найти всех пользователей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Все найденные пользователи")})
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<UserResponseDto>>> getAllUsers() {
        logger.info("GET /api/users - получение всех пользователей");
        List<UserResponseDto> users = userService.findAllUsers();

        List<EntityModel<UserResponseDto>> models = users.stream()
                .map(userResponseDto -> EntityModel.of(userResponseDto,
                        linkTo(methodOn(UserController.class).getUserById(userResponseDto.getId())).withSelfRel()))
                .toList();
        CollectionModel<EntityModel<UserResponseDto>> collectionModel = CollectionModel.of(models,
                linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel());
        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Обновить данные пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные о пользователе успешно обновлены"),
            @ApiResponse(responseCode = "400", description = "Некорректный данные"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponseDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDto userRequestDto) {
        logger.info("PUT /api/users/{} - обновление пользователя", id);
        UserResponseDto updatedUser = userService.updateUser(id, userRequestDto);

        EntityModel<UserResponseDto> model = EntityModel.of(updatedUser,
                linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel(ALL_USERS_REL));
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Удалить пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("DELETE /api/users/{} - удаление пользователя", id);
        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}
