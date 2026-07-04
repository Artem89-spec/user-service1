package com.user_service.service.impl;

import com.user_service.dao.UserDao;
import com.user_service.entity.UserEntity;
import com.user_service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserEntity createUser(String name, String email, String age) {
        logger.info("Сервис: начат процесс создания пользователя c email {}", email);
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email пользователя не может быть пустым");
        }

        if (age == null || age.trim().isEmpty()) {
            throw new IllegalArgumentException("Возраст пользователя не может быть пустым");
        }

        int userIsAge;
        try {
            userIsAge = Integer.parseInt(age.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Некорректный формат возраста");
        }

        if (userIsAge <= 0) {
            throw new IllegalArgumentException("Возраст пользователя не может быть меньше или равен 0");
        }

        if (!isUniqueEmail(email)) {
            throw new RuntimeException("Пользователь с таким email уже существует:" + email);
        }

        logger.info("Сервис: пользователь c email {}  успешно создан", email);
        return userDao.save(new UserEntity(name.trim(), email.trim(), userIsAge));
    }

    @Override
    public Optional<UserEntity> findUserById(Long id) {
        logger.info("Сервис: поиск пользователя по id {}", id);
        if (id == null || id <= 0) {
            logger.error("Был введен некорректный id {}", id);
            throw new IllegalArgumentException("Некорректный id пользователя");
        }

        Optional<UserEntity> user = userDao.findById(id);
        if (user.isPresent()) {
            logger.info("Сервис: пользователь с id {} успешно найден.", id);
        } else {
            logger.info("Сервис: пользователь с id {} не найден.", id);
        }
        return user;
    }

    @Override
    public Optional<UserEntity> findUserByEmail(String email) {
        logger.info("Сервис: поиск пользователя по email {}", email);
        if (email == null || email.trim().isEmpty()) {
            logger.error("Был введен некорректный email {}", email);
            throw new IllegalArgumentException("Некорректный email пользователя");
        }

        Optional<UserEntity> user = userDao.findByEmail(email);
        if (user.isPresent()) {
            logger.info("Сервис: пользователь с email {} успешно найден.", email);
        } else {
            logger.info("Сервис: пользователь с email {} не найден.", email);
        }
        return user;
    }

    @Override
    public List<UserEntity> findUsersByName(String name) {
        logger.info("Сервис: поиск пользователя по имени {}", name);
        if (name == null || name.trim().isEmpty()) {
            logger.error("Было введено некорректное имя {}", name);
            throw new IllegalArgumentException("Некорректное имя пользователя");
        }

        List<UserEntity> users = userDao.findByName(name);
        if (!users.isEmpty()) {
            logger.info("Сервис: пользователи с именем {} успешно найдены.", name);
        } else {
            logger.info("Сервис: пользователи с именем {} не найдены.", name);
        }
        return users;
    }

    @Override
    public List<UserEntity> findAllUsers() {
        logger.info("Сервис: поиск всех пользователей");
        List<UserEntity> users = userDao.findAll();
        if (users == null || users.isEmpty()) {
            logger.info("Сервис: пользователи не найдены.");
        } else {
            logger.info("Сервис: все пользователи успешно найдены.");
        }
        return users;
    }

    @Override
    public UserEntity updateUser(UserEntity user) {
        logger.info("Сервис: обновление информации о пользователе {}", user.getId());

        if (user.getId() == null || user.getId() <= 0) {
            throw new IllegalArgumentException("Некорректный id пользователя");
        }

        Optional<UserEntity> existingUser = userDao.findById(user.getId());
        if (existingUser.isEmpty()) {
            throw new RuntimeException("Пользователь с id " + user.getId() + " не найден.");
        }

        if (!existingUser.get().getEmail().equals(user.getEmail()) && !isUniqueEmail(user.getEmail())) {
            throw new RuntimeException("Пользователь с email " + user.getEmail() + " уже существует.");
        }

        logger.info("Сервис: обновление информации о пользователе {} прошло успешно", user.getId());
        return userDao.update(user);

    }

    @Override
    public boolean deleteUser(Long id) {
        logger.info("Сервис: удаление пользователя с id {}", id);
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Некорректный id пользователя");
        }

        boolean deleted = userDao.delete(id);
        if (deleted) {
            logger.info("Сервис: удаление пользователя с id {} прошло успешно", id);
        } else {
            logger.info("Сервис: пользователь с id {} не удален", id);
        }
        return deleted;
    }

    @Override
    public boolean isUniqueEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return userDao.findByEmail(email).isEmpty();
    }
}
