package com.user_service.repository;

import com.user_service.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Интеграционные тесты UserRepository")
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Сохранение пользователя прошло успешно")
    void save_ShouldPersistUser_WhenSuccessfully() {
        UserEntity newUser = new UserEntity("Test name", "test@email.com", 30);

        UserEntity savedUser = userRepository.save(newUser);

        assertNotNull(savedUser.getId());
        assertEquals("Test name", savedUser.getName());
        assertEquals("test@email.com", savedUser.getEmail());
        assertEquals(30, savedUser.getAge());
        assertNotNull(savedUser.getCreatedAt());

        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals(savedUser.getName(), foundUser.get().getName());
        assertEquals(savedUser.getEmail(), foundUser.get().getEmail());
        assertEquals(savedUser.getAge(), foundUser.get().getAge());
    }

    @Test
    @DisplayName("Попытка сохранения пользователя с уже существующим в базе email вызовет исключение")
    void save_ShouldThrowException_WhenEmailExist() {
        UserEntity oldUser = new UserEntity("Old name", "test@email.com", 30);
        userRepository.save(oldUser);

        UserEntity newUser = new UserEntity("New name", "test@email.com", 30);

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> userRepository.save(newUser));
    }

    @Test
    @DisplayName("Поиск по id прошёл успешно")
    void findById_ShouldReturnUser_WhenExist() {
        UserEntity user = new UserEntity("Name", "email@mail.com", 30);
        UserEntity savedUser = userRepository.save(user);

        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals(savedUser.getName(), foundUser.get().getName());
        assertEquals(savedUser.getEmail(), foundUser.get().getEmail());
        assertEquals(savedUser.getAge(), foundUser.get().getAge());
    }

    @Test
    @DisplayName("Поиск по id - пользователь не найден")
    void findById_ShouldReturnEmpty_WhenNotExist() {
        Optional<UserEntity> foundUser = userRepository.findById(999L);

        assertTrue(foundUser.isEmpty());
    }

    @Test
    @DisplayName("Поиск по имени прошёл успешно")
    void findByName_ShouldReturnUser_WhenExist() {
        userRepository.save(new UserEntity("Name first", "email1@mail.com", 30));
        userRepository.save(new UserEntity("Name second", "email2@mail.com", 30));
        userRepository.save(new UserEntity("Third user", "email3@mail.com", 30));

        List<UserEntity> foundUsers = userRepository.findByNameContainingIgnoreCase("Name");

        assertEquals(2, foundUsers.size());
        assertTrue(foundUsers.stream().allMatch(userEntity ->
                userEntity.getName().contains("Name")));
    }

    @Test
    @DisplayName("Поиск по имени - пользователь не найден")
    void findByName_ShouldReturnEmpty_WhenNotExist() {
        List<UserEntity> foundUsers = userRepository.findByNameContainingIgnoreCase("NoneExistName");

        assertTrue(foundUsers.isEmpty());
    }

    @Test
    @DisplayName("Поиск по email прошёл успешно")
    void findByEmail_ShouldReturnUser_WhenExist() {
        UserEntity user = new UserEntity("Name", "test@email.com", 30);
        UserEntity savedUser = userRepository.save(user);

        Optional<UserEntity> foundUser = userRepository.findByEmail(savedUser.getEmail());

        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals(savedUser.getName(), foundUser.get().getName());
        assertEquals(savedUser.getEmail(), foundUser.get().getEmail());
        assertEquals(savedUser.getAge(), foundUser.get().getAge());
    }

    @Test
    @DisplayName("Поиск по email - пользователь не найден")
    void findByEmail_ShouldReturnEmpty_WhenNotExist() {
        Optional<UserEntity> foundUser = userRepository.findByEmail("NonExistEmail");

        assertTrue(foundUser.isEmpty());
    }

    @Test
    @DisplayName("Поиск всех пользователей")
    void findAll_ShouldReturnAllUsers() {
        userRepository.save(new UserEntity("Name first", "email1@mail.com", 30));
        userRepository.save(new UserEntity("Name second", "email2@mail.com", 30));
        userRepository.save(new UserEntity("Name third", "email3@mail.com", 30));

        List<UserEntity> foundUsers = userRepository.findAll();

        assertEquals(3, foundUsers.size());
        assertTrue(foundUsers.stream().anyMatch(userEntity ->
                "Name first".equals(userEntity.getName())));
        assertTrue(foundUsers.stream().anyMatch(userEntity ->
                "Name second".equals(userEntity.getName())));
        assertTrue(foundUsers.stream().anyMatch(userEntity ->
                "Name third".equals(userEntity.getName())));
    }

    @Test
    @DisplayName("Обновление пользователя прошло успешно")
    void update_ShouldReturnModifiedUser() {
        UserEntity user = new UserEntity("Old name", "old@email.com", 30);
        UserEntity savedUser = userRepository.save(user);

        savedUser.setName("New name");
        savedUser.setEmail("new@email.com");
        savedUser.setAge(35);
        UserEntity updatedUser = userRepository.save(savedUser);

        assertEquals("New name", updatedUser.getName());
        assertEquals("new@email.com", updatedUser.getEmail());
        assertEquals(35, updatedUser.getAge());

        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("New name", foundUser.get().getName());
        assertEquals("new@email.com", foundUser.get().getEmail());
        assertEquals(35, foundUser.get().getAge());
    }

    @Test
    @DisplayName("Удаление пользователя")
    void delete_ShouldRemoveUser() {
        UserEntity user = new UserEntity("Name", "email@mail.com", 30);
        UserEntity savedUser = userRepository.save(user);

        userRepository.deleteById(savedUser.getId());

        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());
        assertTrue(foundUser.isEmpty());

        List<UserEntity> users = userRepository.findAll();
        assertEquals(0, users.size());
        assertTrue(users.isEmpty());
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя - без ошибок")
    void delete_ShouldDoNothing_WhenNotExist() {
        Long nonExistentId = 999L;

        assertDoesNotThrow(() -> userRepository.deleteById(nonExistentId));

        assertEquals(0, userRepository.count());
    }

    @Test
    @DisplayName("existsByEmail - email существует")
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        userRepository.save(new UserEntity("Name", "test@email.com", 30));

        boolean exists = userRepository.existsByEmail("test@email.com");

        assertTrue(exists);
    }

    @Test
    @DisplayName("existsByEmail - email не существует")
    void existsByEmail_ShouldReturnFalse_WhenEmailNotExists() {
        userRepository.save(new UserEntity("Name", "test@email.com", 30));

        boolean exists = userRepository.existsByEmail("nonexistent@email.com");

        assertFalse(exists);
    }
}
