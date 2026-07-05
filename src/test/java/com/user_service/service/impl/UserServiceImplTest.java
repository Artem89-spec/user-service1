package com.user_service.service.impl;

import com.user_service.dto.UserNotificationDto;
import com.user_service.dto.UserRequestDto;
import com.user_service.dto.UserResponseDto;
import com.user_service.entity.UserEntity;
import com.user_service.exception.EmailAlreadyExistsException;
import com.user_service.exception.InvalidUserDataException;
import com.user_service.exception.UserNotFoundException;
import com.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты UserServiceImpl c Mockito ")
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private UserServiceImpl userService;


    private static final String TEST_NAME = "Тестовое имя";
    private static final String TEST_EMAIL = "test@mail.com";
    private static final int TEST_AGE = 30;

    private UserEntity testUser;
    private UserRequestDto testRequestDto;
    private UserResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity(TEST_NAME, TEST_EMAIL, TEST_AGE);
        testUser.setId(1L);
        testUser.setCreatedAt();

        testRequestDto = new UserRequestDto(TEST_NAME, TEST_EMAIL, TEST_AGE);

        testResponseDto = new UserResponseDto(
                1L,
                TEST_NAME,
                TEST_EMAIL,
                TEST_AGE,
                LocalDateTime.now()
        );

        ReflectionTestUtils.setField(userService, "userEventsTopic", "user-events");
    }

    @Test
    @DisplayName("createUser - успешное создание пользователя")
    void createUser_ShouldSaveUser_WhenValid() {
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        UserResponseDto result = userService.createUser(testRequestDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(TEST_NAME, result.getName());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(TEST_AGE, result.getAge());

        verify(userRepository, times(1)).existsByEmail(TEST_EMAIL);
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(kafkaTemplate, times(1)).send(eq("user-events"),
                any(UserNotificationDto.class));
    }

    @Test
    @DisplayName("createUser - уже существующий email вызывает исключение")
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.createUser(testRequestDto)
        );

        assertTrue(exception.getUserMessage().contains(TEST_EMAIL));
        verify(userRepository, times(1)).existsByEmail(TEST_EMAIL);
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("createUser - null DTO вызывает исключение")
    void createUser_ShouldThrowException_WhenDtoIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(null)
        );

        assertEquals("Данные пользователя не могут быть null", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("createUser - пустое имя вызывает исключение")
    void createUser_ShouldThrowException_WhenNameIsEmpty() {
        UserRequestDto invalidDto = new UserRequestDto("", TEST_EMAIL, TEST_AGE);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(invalidDto)
        );

        assertEquals("Имя пользователя не может быть пустым", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("createUser - null имя вызывает исключение")
    void createUser_ShouldThrowException_WhenNameIsNull() {
        UserRequestDto invalidDto = new UserRequestDto(null, TEST_EMAIL, TEST_AGE);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(invalidDto)
        );

        assertEquals("Имя пользователя не может быть пустым", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("createUser - пустой email вызывает исключение")
    void createUser_ShouldThrowException_WhenEmailIsEmpty() {
        UserRequestDto invalidDto = new UserRequestDto(TEST_NAME, "", TEST_AGE);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(invalidDto)
        );

        assertEquals("Email пользователя не может быть пустым", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("createUser - null email вызывает исключение")
    void createUser_ShouldThrowException_WhenEmailIsNull() {
        UserRequestDto invalidDto = new UserRequestDto(TEST_NAME, null, TEST_AGE);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(invalidDto)
        );

        assertEquals("Email пользователя не может быть пустым", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("createUser - null возраст вызывает исключение")
    void createUser_ShouldThrowException_WhenAgeIsNull() {
        UserRequestDto invalidDto = new UserRequestDto(TEST_NAME, TEST_EMAIL, null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(invalidDto)
        );

        assertEquals("Возраст пользователя должен быть больше 0", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("createUser - возраст <= 0 вызывает исключение")
    void createUser_ShouldThrowException_WhenAgeIsIncorrect() {
        UserRequestDto invalidDto = new UserRequestDto(TEST_NAME, TEST_EMAIL, 0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(invalidDto)
        );

        assertEquals("Возраст пользователя должен быть больше 0", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -5, -10})
    @DisplayName("createUser - отрицательный возраст вызывает исключение")
    void createUser_ShouldThrowException_WhenAgeIsNegative(int age) {
        UserRequestDto invalidDto = new UserRequestDto(TEST_NAME, TEST_EMAIL, age);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(invalidDto)
        );

        assertEquals("Возраст пользователя должен быть больше 0", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("findUserById - пользователь успешно найден")
    void findUserById_ShouldReturnUser_WhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<UserResponseDto> result = userService.findUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(TEST_NAME, result.get().getName());
        assertEquals(TEST_EMAIL, result.get().getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findUserById - пользователь не найден")
    void findUserById_ShouldReturnEmpty_WhenNotExists() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<UserResponseDto> result = userService.findUserById(999L);

        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("findUserById - null id вызывает исключение")
    void findUserById_ShouldThrowException_WhenIdIsNull() {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> userService.findUserById(null)
        );

        assertTrue(exception.getUserMessage().contains("Некорректный id"));
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("findUserById - id <= 0 вызывает исключение")
    void findUserById_ShouldThrowException_WhenIdIsIncorrect() {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> userService.findUserById(0L)
        );

        assertTrue(exception.getUserMessage().contains("Некорректный id"));
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("findUserByEmail - пользователь успешно найден")
    void findUserByEmail_ShouldReturnUser_WhenExists() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        Optional<UserResponseDto> result = userService.findUserByEmail(TEST_EMAIL);

        assertTrue(result.isPresent());
        assertEquals(TEST_EMAIL, result.get().getEmail());
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("findUserByEmail - пользователь не найден")
    void findUserByEmail_ShouldReturnEmpty_WhenNotExists() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        Optional<UserResponseDto> result = userService.findUserByEmail(TEST_EMAIL);

        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("findUserByEmail - null email вызывает исключение")
    void findUserByEmail_ShouldThrowException_WhenEmailIsNull() {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> userService.findUserByEmail(null)
        );

        assertTrue(exception.getUserMessage().contains("Некорректный email"));
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("findUserByEmail - пустой email вызывает исключение")
    void findUserByEmail_ShouldThrowException_WhenEmailIsEmpty() {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> userService.findUserByEmail("")
        );

        assertTrue(exception.getUserMessage().contains("Некорректный email"));
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("findUsersByName - пользователи успешно найдены")
    void findUsersByName_ShouldReturnList_WhenExists() {
        List<UserEntity> expectedUsers = Arrays.asList(
                new UserEntity("Иван", "ivan1@mail.com", 25),
                new UserEntity("Иван", "ivan2@mail.com", 30)
        );
        expectedUsers.get(0).setId(1L);
        expectedUsers.get(1).setId(2L);

        when(userRepository.findByNameContainingIgnoreCase("Иван")).thenReturn(expectedUsers);

        List<UserResponseDto> results = userService.findUsersByName("Иван");

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(u -> u.getName().contains("Иван")));
        verify(userRepository, times(1)).findByNameContainingIgnoreCase("Иван");
    }

    @Test
    @DisplayName("findUsersByName - пользователи не найдены")
    void findUsersByName_ShouldReturnEmptyList_WhenNoUsers() {
        when(userRepository.findByNameContainingIgnoreCase("Несуществующее имя")).thenReturn(List.of());

        List<UserResponseDto> results = userService.findUsersByName("Несуществующее имя");

        assertTrue(results.isEmpty());
        verify(userRepository, times(1)).findByNameContainingIgnoreCase("Несуществующее имя");
    }

    @Test
    @DisplayName("findUsersByName - null имя вызывает исключение")
    void findUsersByName_ShouldThrowException_WhenNameIsNull() {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> userService.findUsersByName(null)
        );

        assertTrue(exception.getUserMessage().contains("Некорректное имя"));
        verify(userRepository, never()).findByNameContainingIgnoreCase(any());
    }

    @Test
    @DisplayName("findUsersByName - пустое имя вызывает исключение")
    void findUsersByName_ShouldThrowException_WhenNameIsEmpty() {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> userService.findUsersByName("")
        );

        assertTrue(exception.getUserMessage().contains("Некорректное имя"));
        verify(userRepository, never()).findByNameContainingIgnoreCase(any());
    }

    @Test
    @DisplayName("findAllUsers - все пользователи успешно найдены")
    void findAllUsers_ShouldReturnList_WhenExists() {
        List<UserEntity> expectedUsers = Arrays.asList(
                new UserEntity("User1", "user1@mail.com", 25),
                new UserEntity("User2", "user2@mail.com", 30)
        );
        expectedUsers.get(0).setId(1L);
        expectedUsers.get(1).setId(2L);

        when(userRepository.findAll()).thenReturn(expectedUsers);

        List<UserResponseDto> results = userService.findAllUsers();

        assertEquals(2, results.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAllUsers - пользователи не найдены")
    void findAllUsers_ShouldReturnEmptyList_WhenNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponseDto> results = userService.findAllUsers();

        assertTrue(results.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("updateUser - успешное обновление пользователя")
    void updateUser_ShouldReturnUpdatedUser_WhenExists() {
        UserRequestDto updateDto = new UserRequestDto("Новое имя", "newemail@mail.com", 35);
        UserEntity updatedUser = new UserEntity("Новое имя", "newemail@mail.com", 35);
        updatedUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@mail.com")).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(updatedUser);

        UserResponseDto result = userService.updateUser(1L, updateDto);

        assertNotNull(result);
        assertEquals("Новое имя", result.getName());
        assertEquals("newemail@mail.com", result.getEmail());
        assertEquals(35, result.getAge());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmail("newemail@mail.com");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("updateUser - пользователь не найден")
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        UserRequestDto updateDto = new UserRequestDto("Имя", "email@mail.com", 30);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(999L, updateDto)
        );

        assertTrue(exception.getUserMessage().contains("Пользователь с ID 999 не найден"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUser - null id вызывает исключение")
    void updateUser_ShouldThrowException_WhenIdIsNull() {
        UserRequestDto updateDto = new UserRequestDto("Имя", "email@mail.com", 30);

        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> userService.updateUser(null, updateDto)
        );

        assertTrue(exception.getUserMessage().contains("Некорректный id"));
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("updateUser - email уже занят другим пользователем")
    void updateUser_ShouldThrowException_WhenEmailAlreadyExists() {
        UserRequestDto updateDto = new UserRequestDto("Имя", "existing@mail.com", 30);
        UserEntity existingUser = new UserEntity("Other", "existing@mail.com", 25);
        existingUser.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("existing@mail.com")).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.updateUser(1L, updateDto)
        );

        assertTrue(exception.getUserMessage().contains("existing@mail.com"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteUser - успешное удаление пользователя")
    void deleteUser_ShouldReturnTrue_WhenUserDeleted() {
        UserEntity user = new UserEntity("Test", "test@mail.com", 25);
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(1L);

        boolean deleted = userService.deleteUser(1L);

        assertTrue(deleted);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).deleteById(1L);
        verify(kafkaTemplate, times(1)).send(eq("user-events"),
                any(UserNotificationDto.class));
    }

    @Test
    @DisplayName("deleteUser - пользователь не найден")
    void deleteUser_ShouldReturnFalse_WhenNotExists() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        boolean deleted = userService.deleteUser(999L);

        assertFalse(deleted);
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).deleteById(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("deleteUser - null id вызывает исключение")
    void deleteUser_ShouldThrowException_WhenIdIsNull() {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> userService.deleteUser(null)
        );

        assertTrue(exception.getUserMessage().contains("Некорректный id"));
        verify(userRepository, never()).existsById(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("deleteUser - id <= 0 вызывает исключение")
    void deleteUser_ShouldThrowException_WhenIdIsIncorrect() {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> userService.deleteUser(0L)
        );

        assertTrue(exception.getUserMessage().contains("Некорректный id"));
        verify(userRepository, never()).existsById(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("isUniqueEmail - email свободен")
    void isUniqueEmail_ShouldReturnTrue_WhenNotExists() {
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);

        boolean isUnique = userService.isUniqueEmail(TEST_EMAIL);

        assertTrue(isUnique);
        verify(userRepository, times(1)).existsByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("isUniqueEmail - email уже занят")
    void isUniqueEmail_ShouldReturnFalse_WhenExists() {
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        boolean isUnique = userService.isUniqueEmail(TEST_EMAIL);

        assertFalse(isUnique);
        verify(userRepository, times(1)).existsByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("isUniqueEmail - null email возвращает false")
    void isUniqueEmail_ShouldReturnFalse_WhenEmailIsNull() {
        boolean isUnique = userService.isUniqueEmail(null);

        assertFalse(isUnique);
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    @DisplayName("isUniqueEmail - пустой email возвращает false")
    void isUniqueEmail_ShouldReturnFalse_WhenEmailIsEmpty() {
        boolean isUnique = userService.isUniqueEmail("");

        assertFalse(isUnique);
        verify(userRepository, never()).existsByEmail(any());
    }
}
