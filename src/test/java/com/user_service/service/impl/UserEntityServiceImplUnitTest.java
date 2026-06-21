package com.user_service.service.impl;

import com.user_service.dao.UserDao;
import com.user_service.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты UserServiceImpl c Mockito ")
public class UserEntityServiceImplUnitTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    private static final String TEST_NAME = "Тестовое имя";
    private static final String TEST_EMAIL = "Тестовый адрес";
    private static final String TEST_AGE = "30";
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity(TEST_NAME, TEST_EMAIL, 30);
        testUser.setId(1L);
    }

    @Test
    @DisplayName("createUser - успешное создание пользователя")
    void createUser_ShouldSaveUser_WhenValid() {
        when(userDao.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.empty());

        when(userDao.save(any(UserEntity.class)))
                .thenReturn(testUser);

        UserEntity result = userService.createUser(TEST_NAME, TEST_EMAIL, TEST_AGE);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(TEST_NAME, result.getName());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(30, result.getAge());
    }

    @Test
    @DisplayName("createUser - null имя вызывает исключение")
    void createUser_ShouldThrowException_WhenNameIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(null, TEST_EMAIL, TEST_AGE));

        verify(userDao, never()).findByEmail(anyString());
        verify(userDao, never()).save(any());
    }

    @Test
    @DisplayName("createUser - пустое имя вызывает исключение")
    void createUser_ShouldThrowException_WhenNameIsEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("", TEST_EMAIL, TEST_AGE));

        assertEquals("Имя пользователя не может быть пустым", exception.getMessage());
        verify(userDao, never()).findByEmail(anyString());
        verify(userDao, never()).save(any());
    }

    @Test
    @DisplayName("createUser - null email вызывает исключение")
    void createUser_ShouldThrowException_WhenEmailIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(TEST_NAME, null, TEST_AGE));

        verify(userDao, never()).findByEmail(anyString());
        verify(userDao, never()).save(any());
    }

    @Test
    @DisplayName("createUser - пустой email вызывает исключение")
    void createUser_ShouldThrowException_WhenEmailIsEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(TEST_NAME, "", TEST_AGE));

        assertEquals("Email пользователя не может быть пустым", exception.getMessage());
        verify(userDao, never()).findByEmail(anyString());
        verify(userDao, never()).save(any());
    }

    @Test
    @DisplayName("createUser - null возраст вызывает исключение")
    void createUser_ShouldThrowException_WhenAgeIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(TEST_NAME, TEST_EMAIL, null));

        verify(userDao, never()).findByEmail(anyString());
        verify(userDao, never()).save(any());
    }

    @Test
    @DisplayName("createUser - пустой возраст вызывает исключение")
    void createUser_ShouldThrowException_WhenAgeIsEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(TEST_NAME, TEST_EMAIL, ""));

        assertEquals("Возраст пользователя не может быть пустым", exception.getMessage());
        verify(userDao, never()).findByEmail(anyString());
        verify(userDao, never()).save(any());
    }

    @Test
    @DisplayName("createUser - некорректный формат возраста вызывает исключение")
    void createUser_ShouldThrowException_WhenAgeIsInvalidFormat() {
        NumberFormatException exception = assertThrows(NumberFormatException.class,
                () -> userService.createUser(TEST_NAME, TEST_EMAIL, "тридцать"));

        assertEquals("Некорректный формат возраста", exception.getMessage());
        verify(userDao, never()).findByEmail(anyString());
        verify(userDao, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-100"})
    @DisplayName("createUser - некорректный возраст вызывает исключение")
    void createUser_ShouldThrowException_WhenAgeIsIncorrect(String incorrectAge) {
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(TEST_NAME, TEST_EMAIL, incorrectAge));

        verify(userDao, never()).findByEmail(anyString());
        verify(userDao, never()).save(any());
    }

    @Test
    @DisplayName("createUser - уже существующий email вызывает исключение")
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        when(userDao.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.createUser(TEST_NAME, TEST_EMAIL, TEST_AGE));

        assertEquals("Пользователь с таким email уже существует:" + TEST_EMAIL, exception.getMessage());
        verify(userDao, times(1)).findByEmail(TEST_EMAIL);
        verify(userDao, never()).save(any());
    }

    @Test
    @DisplayName("findUserById - пользователь успешно найден")
    void findUserById_ShouldReturnUser_WhenExists() {
        when(userDao.findById(1L))
                .thenReturn(Optional.of(testUser));

        Optional<UserEntity> result = userService.findUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(testUser.getName(), result.get().getName());
        verify(userDao, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findUserById - пользователь не найден")
    void findUserById_ShouldReturnEmpty_WhenNotExists() {
        when(userDao.findById(100L))
                .thenReturn(Optional.empty());

        Optional<UserEntity> result = userService.findUserById(100L);

        assertTrue(result.isEmpty());
        verify(userDao, times(1)).findById(100L);
    }

    @Test
    @DisplayName("findUserById - null id вызывает исключение")
    void findUserById_ShouldThrowException_WhenIdIsNull() {

        assertThrows(IllegalArgumentException.class,
                () -> userService.findUserById(null));

        verify(userDao, never()).findById(any());
    }

    @Test
    @DisplayName("findUserById - id <= 0 вызывает исключение")
    void findUserById_ShouldThrowException_WhenIdIsIncorrect() {

        assertThrows(IllegalArgumentException.class,
                () -> userService.findUserById(0L));

        assertThrows(IllegalArgumentException.class,
                () -> userService.findUserById(-1L));

        verify(userDao, never()).findById(any());
    }

    @Test
    @DisplayName("findUsersByName - пользователь успешно найден по имени")
    void findUsersByName_ShouldReturnListUsers_WhenExists() {
        List<UserEntity> expectedUsers = Arrays.asList(
                new UserEntity("1", "3", 1),
                new UserEntity("1", "4", 1)
        );

        when(userDao.findByName("1")).thenReturn(expectedUsers);

        List<UserEntity> results = userService.findUsersByName("1");

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(userEntity ->
                userEntity.getName().contains("1")));
        verify(userDao, times(1)).findByName("1");
    }

    @Test
    @DisplayName("findUsersByName - пользователи не найдены по имени")
    void findUsersByName_ShouldReturnEmptyListUsers_WhenNoUsers() {
        when(userDao.findByName("1")).thenReturn(List.of());

        List<UserEntity> results = userService.findUsersByName("1");

        assertTrue(results.isEmpty());
        verify(userDao, times(1)).findByName("1");
    }

    @Test
    @DisplayName("findUsersByName - null name вызывает исключение")
    void findUsersByName_ShouldThrowException_WhenNameIsNull() {

        assertThrows(IllegalArgumentException.class,
                () -> userService.findUsersByName(null));

        verify(userDao, never()).findByName(anyString());
    }

    @Test
    @DisplayName("findUsersByName - пустое имя вызывает исключение")
    void findUsersByName_ShouldThrowException_WhenNameIsEmpty() {

        assertThrows(IllegalArgumentException.class,
                () -> userService.findUsersByName(""));

        verify(userDao, never()).findByName(anyString());
    }

    @Test
    @DisplayName("findUserByEmail - пользователь успешно найден")
    void findUserByEmail_ShouldReturnUser_WhenExists() {
        when(userDao.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));

        Optional<UserEntity> result = userService.findUserByEmail(TEST_EMAIL);

        assertTrue(result.isPresent());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        verify(userDao, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("findUserByEmail - пользователь не найден")
    void findUserByEmail_ShouldReturnEmpty_WhenNotExists() {
        when(userDao.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.empty());

        Optional<UserEntity> result = userService.findUserByEmail(TEST_EMAIL);

        assertTrue(result.isEmpty());
        verify(userDao, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("findUserByEmail - null email вызывает исключение")
    void findUserByEmail_ShouldThrowException_WhenEmailIsNull() {

        assertThrows(IllegalArgumentException.class,
                () -> userService.findUserByEmail(null));

        verify(userDao, never()).findByEmail(any());
    }

    @Test
    @DisplayName("findUserByEmail - пустой email вызывает исключение")
    void findUserByEmail_ShouldThrowException_WhenEmailIsEmpty() {

        assertThrows(IllegalArgumentException.class,
                () -> userService.findUserByEmail(""));

        verify(userDao, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("findAllUsers - все пользователи успешно найдены")
    void findAllUsers_ShouldReturnListAllUsers_WhenExists() {
        List<UserEntity> expectedUsers = Arrays.asList(
                new UserEntity("1", "3", 1),
                new UserEntity("1", "4", 1)
        );

        when(userDao.findAll()).thenReturn(expectedUsers);

        List<UserEntity> results = userService.findAllUsers();

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(userEntity ->
                userEntity.getName().contains("1")));
        verify(userDao, times(1)).findAll();
    }

    @Test
    @DisplayName("findAllUsers - пользователи не найдены")
    void findAllUsers_ShouldReturnEmptyListUsers_WhenNoUsers() {
        when(userDao.findAll()).thenReturn(List.of());

        List<UserEntity> results = userService.findAllUsers();

        assertTrue(results.isEmpty());
        verify(userDao, times(1)).findAll();
    }

    @Test
    @DisplayName("updateUser - успешное обновление пользователя")
    void updateUser_ShouldReturnUpdatedUser_WhenExists() {
        UserEntity updatedUser = new UserEntity("New name", "New email", 30);
        updatedUser.setId(1L);

        when(userDao.findById(1L))
                .thenReturn(Optional.of(testUser));

        when(userDao.findByEmail("New email"))
                .thenReturn(Optional.empty());

        when(userDao.update(any(UserEntity.class)))
                .thenReturn(updatedUser);

        UserEntity result = userService.updateUser(updatedUser);

        assertNotNull(result);
        assertEquals("New name", result.getName());
        assertEquals("New email", result.getEmail());
        assertEquals(30, result.getAge());

        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).findByEmail("New email");
        verify(userDao, times(1)).update(any(UserEntity.class));
    }

    @Test
    @DisplayName("updateUser - пользователь не найден")
    void updateUser_ShouldThrowException_WhenNotExists() {
        UserEntity nonExistingUser = new UserEntity("name", "email", 30);
        nonExistingUser.setId(100L);

        when(userDao.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.updateUser(nonExistingUser));

        assertEquals("Пользователь с id " + 100L + " не найден.", exception.getMessage());

        verify(userDao, never()).update(any());
    }

    @Test
    @DisplayName("updateUser - null id вызывает исключение")
    void updateUser_ShouldThrowException_WhenIdIsNull() {
        UserEntity userWithNullId = new UserEntity("name", "email", 30);
        userWithNullId.setId(null);

        assertThrows(IllegalArgumentException.class, () ->
                userService.updateUser(userWithNullId));

        verify(userDao, never()).findById(any());
        verify(userDao, never()).update(any());
    }

    @Test
    @DisplayName("deleteUser - успешное удаление пользователя")
    void deleteUser_ShouldReturnTrue_WhenUserDeleted() {
      when(userDao.delete(1L)).thenReturn(true);

      boolean deletedUser = userService.deleteUser(1L);

      assertTrue(deletedUser);

      verify(userDao, times(1)).delete(1L);
    }

    @Test
    @DisplayName("deleteUser - пользователь не найден")
    void deleteUser_ShouldReturnFalse_WhenNotExists() {
        when(userDao.delete(1L)).thenReturn(false);

        boolean deletedUser = userService.deleteUser(1L);

        assertFalse(deletedUser);

        verify(userDao, times(1)).delete(1L);
    }

    @Test
    @DisplayName("deleteUser - null id вызывает исключение")
    void deleteUser_ShouldThrowException_WhenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.deleteUser(null));

        verify(userDao, never()).delete(any());
    }

    @Test
    @DisplayName("isUniqueEmail - email свободен")
    void isUniqueEmail_ShouldReturnTrue_WhenNotExists() {
        when(userDao.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        boolean isUnique = userService.isUniqueEmail(TEST_EMAIL);

        assertTrue(isUnique);

        verify(userDao, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("isUniqueEmail - email уже занят")
    void isUniqueEmail_ShouldReturnFalse_WhenExists() {
        when(userDao.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        boolean isUnique = userService.isUniqueEmail(TEST_EMAIL);

        assertFalse(isUnique);

        verify(userDao, times(1)).findByEmail(TEST_EMAIL);
    }
}
