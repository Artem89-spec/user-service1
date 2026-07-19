package com.user_service.console;

import com.user_service.dto.UserRequestDto;
import com.user_service.dto.UserResponseDto;
import com.user_service.exception.EmailAlreadyExistsException;
import com.user_service.exception.InvalidUserDataException;
import com.user_service.exception.UserNotFoundException;
import com.user_service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Component
public class ConsoleInterface {

    private final UserService userService;
    private final Scanner scanner;
    private static final Logger logger = LoggerFactory.getLogger(ConsoleInterface.class);

    public ConsoleInterface(UserService userService) {
        this.userService = userService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        while (true) {
            try {
                printMenu();
                int choice = readInt("Выберите действие: ");

                switch (choice) {
                    case 1 -> createUser();
                    case 2 -> findUserById();
                    case 3 -> findUserByEmail();
                    case 4 -> findUsersByName();
                    case 5 -> findAllUsers();
                    case 6 -> updateUser();
                    case 7 -> deleteUser();
                    case 8 -> {
                        logger.info("Выход из программы");
                        return;
                    }
                    default -> logger.warn("Некорректный выбор: {}", choice);
                }
            } catch (UserNotFoundException e) {
                logger.error("Пользователь не найден: {}", e.getMessage());
            } catch (EmailAlreadyExistsException e) {
                logger.error("Email уже занят: {}", e.getMessage());
            } catch (InvalidUserDataException e) {
                logger.error("Некорректные данные: {}", e.getUserMessage());
            } catch (IllegalArgumentException e) {
                logger.error("Ошибка ввода: {}", e.getMessage());
            } catch (RuntimeException e) {
                logger.error("Ошибка:", e);
            }
        }
    }

    public void printMenu() {
        logger.info("======= Меню =======");
        logger.info("1. Создать пользователя");
        logger.info("2. Найти пользователя по id");
        logger.info("3. Найти пользователя по email");
        logger.info("4. Найти пользователя по имени");
        logger.info("5. Найти всех пользователей");
        logger.info("6. Изменить данные о пользователе");
        logger.info("7. Удалить пользователя");
        logger.info("8. Выход");
    }

    private int readInt(String message) {
        logger.info(message);
        return Integer.parseInt(scanner.nextLine());
    }

    private String readString(String message) {
        logger.info(message);
        return scanner.nextLine();
    }

    private void createUser() {
        String name = readString("Введите имя нового пользователя");
        String email = readString("Введите email нового пользователя");
        String strAge = readString("Введите возраст нового пользователя");

        UserRequestDto requestDto = new UserRequestDto(name, email, Integer.parseInt(strAge));
        UserResponseDto createdUser = userService.createUser(requestDto);
        logger.info("Создание пользователя прошло успешно: {}", createdUser);
    }

    private void findUserById() {
        Long id = (long) readInt("Введите id пользователя");
        UserResponseDto user = userService.findUserById(id);
        logger.info("Найден пользователь по id {}: {}", id, user);
    }

    private void findUserByEmail() {
        String email = readString("Введите email пользователя");
        if (email.trim().isEmpty()) {
            throw new InvalidUserDataException("email", "Email не может быть пустым");
        }

        UserResponseDto user = userService.findUserByEmail(email);
        logger.info("Найден пользователь по email {}: {}", email, user);
    }

    private void findUsersByName() {
        String name = readString("Введите имя пользователя");
        if (name.trim().isEmpty()) {
            throw new InvalidUserDataException("name", "Имя не может быть пустым");
        }

        List<UserResponseDto> users = userService.findUsersByName(name);
        if (!users.isEmpty()) {
            users.forEach(user -> logger.info("{}", user));
        } else {
            logger.info("Пользователи с именем {} не найдены", name);
        }
    }

    private void findAllUsers() {
        List<UserResponseDto> results = userService.findAllUsers();
        if (!results.isEmpty()) {
            results.forEach(user -> logger.info("{}", user));
        } else {
            logger.info("Пользователи отсутствуют");
        }
    }

    private void updateUser() {
        Long id = (long) readInt("Введите id пользователя");

        UserResponseDto oldUser = userService.findUserById(id);
        logger.info("Текущие данные пользователя: {}", oldUser);

        String name = readString("Введите новое имя пользователя");
        String email = readString("Введите новый email пользователя");
        String strAge = readString("Введите новый возраст пользователя");

        String newName = name.isEmpty() ? oldUser.getName() : name;
        String newEmail = email.isEmpty() ? oldUser.getEmail() : email;
        int newAge = strAge.isEmpty() ? oldUser.getAge() : Integer.parseInt(strAge);

        UserRequestDto requestDto = new UserRequestDto(newName, newEmail, newAge);
        UserResponseDto updatedUser = userService.updateUser(id, requestDto);
        logger.info("Пользователь успешно обновлен: {}", updatedUser);
    }

    private void deleteUser() {
        Long id = (long) readInt("Введите id пользователя: ");

        userService.deleteUser(id);
        logger.info("Пользователь c id {} успешно удален", id);
    }
}
