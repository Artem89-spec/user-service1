package com.user_service.console;

import com.user_service.dao.impl.UserDaoImpl;
import com.user_service.entity.UserEntity;
import com.user_service.service.UserService;
import com.user_service.service.impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleInterface {

    private UserService userService;
    private Scanner scanner;
    private static final Logger logger = LoggerFactory.getLogger(ConsoleInterface.class);

    public ConsoleInterface() {
        this.userService = new UserServiceImpl(new UserDaoImpl());
        this.scanner = new Scanner(System.in);
    }

    public void start() {

        while (true) {
            printMenu();
            int choice = readInt("Выберите действие: ");

            switch (choice) {
                case 1:
                    createUser();
                    break;
                case 2:
                    findUserById();
                    break;
                case 3:
                    findUserByEmail();
                    break;
                case 4:
                    findUsersByName();
                    break;
                case 5:
                    findAllUsers();
                    break;
                case 6:
                    updateUser();
                    break;
                case 7:
                    deleteUser();
                    break;
                case 8:
                    System.out.println("Выход");
                    return;
                default:
                    System.out.println("Некорректный выбор");
            }
        }
    }

    public void printMenu() {
        System.out.println("======= Меню =======");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Найти пользователя по id");
        System.out.println("3. Найти пользователя по email");
        System.out.println("4. Найти пользователя по имени");
        System.out.println("5. Найти всех пользователей");
        System.out.println("6. Изменить данные о пользователе");
        System.out.println("7. Удалить пользователя");
        System.out.println("8. Выход");
    }

    private int readInt(String message) {
        System.out.println(message);
        return Integer.parseInt(scanner.nextLine());
    }

    private String readString(String message) {
        System.out.println(message);
        return scanner.nextLine();
    }

    private void createUser() {
        try {
            String name = readString("Введите имя нового пользователя");
            String email = readString("Введите email нового пользователя");
            String strAge = readString("Введите возраст нового пользователя");

            UserEntity newUser = userService.createUser(name, email, strAge);
            System.out.println("Пользователь успешно создан: " + newUser);
            logger.info("Создание пользователя прошло успешно: {}", newUser);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
            logger.error("Ошибка в связи с некорректным аргументом: {}", e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Ошибка: " + e.getMessage());
            logger.error("Ошибка: {}", e.getMessage());
        }
    }

    private void findUserById() {
        Long id = (long) readInt("Введите id пользователя");

        Optional<UserEntity> userOptional = userService.findUserById(id);
        if (userOptional.isPresent()) {
            System.out.println(userOptional.get());
        } else {
            System.out.printf("Пользователь с id %d не найден\n", id);
        }
    }

    private void findUserByEmail() {
        String email = readString("Введите email пользователя");

        if (email.trim().isEmpty()) {
            System.out.println("Email не может быть пустым");
            return;
        }

        Optional<UserEntity> userOptional = userService.findUserByEmail(email);
        if (userOptional.isPresent()) {
            System.out.println(userOptional.get());
        } else {
            System.out.printf("Пользователь с email %s не найден\n", email);
        }
    }

    private void findUsersByName() {
        String name = readString("Введите имя пользователя");

        if (name.trim().isEmpty()) {
            System.out.println("Имя не может быть пустым");
            return;
        }

        List<UserEntity> users = userService.findUsersByName(name);
        if (!users.isEmpty()) {
            for (UserEntity user : users) {
                System.out.println(user);
            }
        } else {
            System.out.printf("Пользователи с именем %s не найдены\n", name);
        }
    }

    private void findAllUsers() {
        List<UserEntity> results = userService.findAllUsers();
        if (!results.isEmpty()) {
            results.forEach(System.out::println);
        } else {
            System.out.println("Пользователи отсутствуют");
        }
    }

    private void updateUser() {
        try {
            Long id = (long) readInt("Введите id пользователя");

            Optional<UserEntity> userOptional = userService.findUserById(id);
            if (userOptional.isPresent()) {
                String name = readString("Введите новое имя пользователя");
                String email = readString("Введите новый email пользователя");
                String strAge = readString("Введите новый возраст пользователя");

                UserEntity user = userOptional.get();

                if (!name.isEmpty()) user.setName(name);
                if (!email.isEmpty()) user.setEmail(email);
                if (!strAge.isEmpty()) {
                    try {
                        int age = Integer.parseInt(strAge);
                        if (age <= 0) {
                            System.out.println("Возраст должен быть положительным");
                        } else {
                            user.setAge(age);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Некорректный формат возраста");
                    }
                }

                userService.updateUser(user);
                System.out.println("Пользователь успешно обновлен: " + user);
                logger.info("Пользователь успешно обновлен: {}", user);
            } else {
                System.out.printf("Пользователь с id %d не найден\n", id);
                logger.info("Пользователь не найден по id: {}", id);
            }
        } catch (RuntimeException e) {
            System.out.println("Ошибка: " + e.getMessage());
            logger.error("Возникла ошибка при обновлении информации о пользователе: {}", e.getMessage());
        }

    }

    private void deleteUser() {
        Long id = (long) readInt("Введите id пользователя: ");

        Optional<UserEntity> userOptional = userService.findUserById(id);
        if (userOptional.isPresent()) {
            userService.deleteUser(id);
            System.out.println("Пользователь успешно удален");
            logger.info("Пользователь успешно удален: {}", userOptional.get());
        } else {
            System.out.printf("Пользователь с id %d не найден\n", id);
            logger.info("Пользователь не был удален по id: {}", id);
        }
    }
}
