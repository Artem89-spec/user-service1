package com.user_service.dao.impl;

import com.user_service.entity.UserEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDaoImplIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    private static SessionFactory sessionFactory;
    private UserDaoImpl userDao;

    @BeforeAll
    static void setUpAll() {
        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        configuration.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgres.getUsername());
        configuration.setProperty("hibernate.connection.password", postgres.getPassword());
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.format_sql", "true");
        configuration.addAnnotatedClass(UserEntity.class);

        sessionFactory = configuration.buildSessionFactory();

        System.out.println("Testcontainers PostgreSQL запущен");
        System.out.println("SessionFactory создана для тестов");
    }

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl(sessionFactory);
        cleanDatabase();
    }

    private void cleanDatabase() {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY", Void.class).executeUpdate();
            transaction.commit();
        }
    }

    @AfterAll
    static void closingAll() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
        System.out.println("Тесты завершены, SessionFactory закрыта");
    }

    @Test
    @Order(1)
    @DisplayName("Сохранение пользователя прошло успешно")
    void save_ShouldPersistUser_WhenSuccessfully() {
        UserEntity newUser = new UserEntity("Test name", "Test email", 30);

        UserEntity savedUser = userDao.save(newUser);

        assertNotNull(savedUser.getId());
        assertEquals("Test name", savedUser.getName());
        assertEquals("Test email", savedUser.getEmail());
        assertEquals(30, savedUser.getAge());

        Optional<UserEntity> foundUser = userDao.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
    }

    @Test
    @Order(2)
    @DisplayName("Попытка сохранения пользователя с уже существующим в базе email вызовет исключение")
    void save_ShouldThrowException_WhenEmailExist() {
        UserEntity oldUser = new UserEntity("Old name", "Email", 30);
        UserEntity newUser = new UserEntity("New name", "Email", 30);

        userDao.save(oldUser);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userDao.save(newUser));

        assertTrue(exception.getMessage().contains("Email уже существует в базе: "));
    }

    @Test
    @Order(3)
    @DisplayName("Поиск по id прошёл успешно")
    void findById_ShouldReturnUser_WhenExist() {
        UserEntity user = new UserEntity("Name", "Email", 30);
        UserEntity savedUser = userDao.save(user);

        Optional<UserEntity> foundUser = userDao.findById(savedUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals(savedUser.getName(), foundUser.get().getName());
        assertEquals(savedUser.getEmail(), foundUser.get().getEmail());
        assertEquals(savedUser.getAge(), foundUser.get().getAge());
    }

    @Test
    @Order(4)
    @DisplayName("Поиск по id - пользователь не найден")
    void findById_ShouldReturnEmpty_WhenNotExist() {
        Optional<UserEntity> foundUser = userDao.findById(100L);

        assertTrue(foundUser.isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("Поиск по имени прошёл успешно")
    void findByName_ShouldReturnUser_WhenExist() {
        userDao.save(new UserEntity("Name first", "Email1", 30));
        userDao.save(new UserEntity("Name second", "Email2", 30));
        userDao.save(new UserEntity("Third user", "Email3", 30));

        List<UserEntity> foundUsers = userDao.findByName("Name");

        assertEquals(2, foundUsers.size());
        assertTrue(foundUsers.stream().allMatch(userEntity ->
                userEntity.getName().contains("Name")));
    }

    @Test
    @Order(6)
    @DisplayName("Поиск по имени - пользователь не найден")
    void findByNane_ShouldReturnEmpty_WhenNotExist() {
        List<UserEntity> foundUsers = userDao.findByName("NoneExistName");

        assertTrue(foundUsers.isEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("Поиск по email прошёл успешно")
    void findByEmail_ShouldReturnUser_WhenExist() {
        UserEntity user = new UserEntity("Name", "Email", 30);
        UserEntity savedUser = userDao.save(user);

        Optional<UserEntity> foundUser = userDao.findByEmail(savedUser.getEmail());

        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals(savedUser.getName(), foundUser.get().getName());
        assertEquals(savedUser.getEmail(), foundUser.get().getEmail());
        assertEquals(savedUser.getAge(), foundUser.get().getAge());
    }

    @Test
    @Order(8)
    @DisplayName("Поиск по email - пользователь не найден")
    void findByEmail_ShouldReturnEmpty_WhenNotExist() {
        Optional<UserEntity> foundUser = userDao.findByEmail("NonExistEmail");

        assertTrue(foundUser.isEmpty());
    }

    @Test
    @Order(9)
    @DisplayName("Поиск всех пользователей")
    void findAll_ShouldReturnAllUsers() {
        userDao.save(new UserEntity("Name first", "Email1", 30));
        userDao.save(new UserEntity("Name second", "Email2", 30));
        userDao.save(new UserEntity("Name third", "Email3", 30));

        List<UserEntity> foundUsers = userDao.findAll();

        assertEquals(3, foundUsers.size());
        assertTrue(foundUsers.stream().anyMatch(userEntity ->
                "Name first".equals(userEntity.getName())));
        assertTrue(foundUsers.stream().anyMatch(userEntity ->
                "Name second".equals(userEntity.getName())));
        assertTrue(foundUsers.stream().anyMatch(userEntity ->
                "Name third".equals(userEntity.getName())));
    }

    @Test
    @Order(10)
    @DisplayName("Обновление пользователя прошло успешно")
    void update_ShouldReturnModifyUser() {
        UserEntity user = new UserEntity("Old name", "Old email", 30);
        UserEntity savedUser = userDao.save(user);

        savedUser.setName("New name");
        savedUser.setEmail("New email");
        savedUser.setAge(35);
        UserEntity updatedUser = userDao.update(savedUser);

        assertEquals("New name", updatedUser.getName());
        assertEquals("New email", updatedUser.getEmail());
        assertEquals(35, updatedUser.getAge());

        Optional<UserEntity> foundedUser = userDao.findById(savedUser.getId());

        assertTrue(foundedUser.isPresent());
        assertEquals("New name", foundedUser.get().getName());
        assertEquals("New email", foundedUser.get().getEmail());
        assertEquals(35, foundedUser.get().getAge());
    }

    @Test
    @Order(11)
    @DisplayName("Удаление пользователя")
    void delete_ShouldRemoveUser() {
        UserEntity user = new UserEntity("Name", "Email", 30);
        UserEntity savedUser = userDao.save(user);

        boolean deleted = userDao.delete(savedUser.getId());

        assertTrue(deleted);

        Optional<UserEntity> foundUser = userDao.findById(savedUser.getId());
        assertTrue(foundUser.isEmpty());

        List<UserEntity> users = userDao.findAll();
        assertEquals(0, users.size());
        assertTrue(users.isEmpty());
    }

    @Test
    @Order(12)
    @DisplayName("Удаление несуществующего пользователя")
    void delete_ShouldReturnFalse_WhenNotExist() {
      boolean deleted = userDao.delete(100L);

      assertFalse(deleted);
    }
}
