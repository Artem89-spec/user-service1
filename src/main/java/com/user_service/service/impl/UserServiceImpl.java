    package com.user_service.service.impl;

    import com.user_service.dto.UserRequestDto;
    import com.user_service.dto.UserResponseDto;
    import com.user_service.entity.UserEntity;
    import com.user_service.exception.EmailAlreadyExistsException;
    import com.user_service.exception.InvalidUserDataException;
    import com.user_service.exception.UserNotFoundException;
    import com.user_service.repository.UserRepository;
    import com.user_service.service.UserService;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.util.List;
    import java.util.Optional;

    @Service
    @Transactional
    public class UserServiceImpl implements UserService {

        private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

        private final UserRepository userRepository;

        @Autowired
        public UserServiceImpl(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        @Override
        public UserResponseDto createUser(UserRequestDto userRequestDto) {

            validateUserRequest(userRequestDto);

            logger.info("Сервис: начат процесс создания пользователя c email {}", userRequestDto.getEmail());

            if (userRepository.existsByEmail(userRequestDto.getEmail())) {
                throw new EmailAlreadyExistsException(userRequestDto.getEmail());
            }

            UserEntity user = convertToEntity(userRequestDto);
            UserEntity savedUser = userRepository.save(user);

            logger.info("Сервис: пользователь c email {}  успешно создан", savedUser.getEmail());
            return convertToResponseDto(savedUser);
        }

        @Override
        public Optional<UserResponseDto> findUserById(Long id) {
            logger.info("Сервис: поиск пользователя по id {}", id);
            if (id == null || id <= 0) {
                logger.error("Был введен некорректный id {}", id);
                throw new InvalidUserDataException("id", "Некорректный id пользователя");
            }

            Optional<UserEntity> user = userRepository.findById(id);
            if (user.isPresent()) {
                logger.info("Сервис: пользователь с id {} успешно найден.", id);
            } else {
                logger.info("Сервис: пользователь с id {} не найден.", id);
            }
            return user.map(this::convertToResponseDto);
        }

        @Override
        public Optional<UserResponseDto> findUserByEmail(String email) {
            logger.info("Сервис: поиск пользователя по email {}", email);
            if (email == null || email.trim().isEmpty()) {
                logger.error("Был введен некорректный email {}", email);
                throw new InvalidUserDataException("email", "Некорректный email пользователя");
            }

            Optional<UserEntity> user = userRepository.findByEmail(email);
            if (user.isPresent()) {
                logger.info("Сервис: пользователь с email {} успешно найден.", email);
            } else {
                logger.info("Сервис: пользователь с email {} не найден.", email);
            }
            return user.map(this::convertToResponseDto);
        }

        @Override
        public List<UserResponseDto> findUsersByName(String name) {
            logger.info("Сервис: поиск пользователя по имени {}", name);
            if (name == null || name.trim().isEmpty()) {
                logger.error("Было введено некорректное имя {}", name);
                throw new InvalidUserDataException("name", "Некорректное имя пользователя");
            }

            List<UserEntity> users = userRepository.findByNameContainingIgnoreCase(name);
            if (!users.isEmpty()) {
                logger.info("Сервис: пользователи с именем {} успешно найдены.", name);
            } else {
                logger.info("Сервис: пользователи с именем {} не найдены.", name);
            }
            return users.stream()
                    .map(this::convertToResponseDto)
                    .toList();
        }

        @Override
        public List<UserResponseDto> findAllUsers() {
            logger.info("Сервис: поиск всех пользователей");
            List<UserEntity> users = userRepository.findAll();
            if (users.isEmpty()) {
                logger.info("Сервис: пользователи не найдены.");
            } else {
                logger.info("Сервис: все пользователи успешно найдены.");
            }
            return users.stream().
                    map(this::convertToResponseDto)
                    .toList();
        }

        @Override
        public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto) {
            logger.info("Сервис: обновление информации о пользователе {}", id);

            if (id == null || id <= 0) {
                throw new InvalidUserDataException("id", "Некорректный id пользователя");
            }

            validateUserRequest(userRequestDto);

            UserEntity existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException(id));

            if (!existingUser.getEmail().equals(userRequestDto.getEmail())
                    && userRepository.existsByEmail(userRequestDto.getEmail())) {
                throw new EmailAlreadyExistsException(userRequestDto.getEmail());
            }

            existingUser.setName(userRequestDto.getName());
            existingUser.setEmail(userRequestDto.getEmail());
            existingUser.setAge(userRequestDto.getAge());
            UserEntity updatedUser = userRepository.save(existingUser);
            logger.info("Сервис: обновление информации о пользователе {} прошло успешно", updatedUser.getId());
            return convertToResponseDto(updatedUser);

        }

        @Override
        public boolean deleteUser(Long id) {
            logger.info("Сервис: удаление пользователя с id {}", id);
            if (id == null || id <= 0) {
                throw new InvalidUserDataException("id", "Некорректный id пользователя");
            }

            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                logger.info("Сервис: удаление пользователя с id {} прошло успешно", id);
                return true;
            }

            logger.info("Сервис: пользователь с id {} не удален", id);
            return false;
        }

        @Override
        public boolean isUniqueEmail(String email) {
            if (email == null || email.trim().isEmpty()) {
                return false;
            }
            return !userRepository.existsByEmail(email);
        }

        private void validateUserRequest(UserRequestDto userRequestDto) {
            if (userRequestDto == null) {
                throw new IllegalArgumentException("Данные пользователя не могут быть null");
            }

            if (userRequestDto.getName() == null || userRequestDto.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Имя пользователя не может быть пустым");
            }

            if (userRequestDto.getEmail() == null || userRequestDto.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email пользователя не может быть пустым");
            }

            if (userRequestDto.getAge() == null || userRequestDto.getAge() <= 0) {
                throw new IllegalArgumentException("Возраст пользователя должен быть больше 0");
            }
        }

        private UserEntity convertToEntity(UserRequestDto userRequestDto) {
            return new UserEntity(
                    userRequestDto.getName().trim(),
                    userRequestDto.getEmail().trim(),
                    userRequestDto.getAge()
            );
        }

        private UserResponseDto convertToResponseDto(UserEntity userEntity) {
            return new UserResponseDto(
                    userEntity.getId(),
                    userEntity.getName(),
                    userEntity.getEmail(),
                    userEntity.getAge(),
                    userEntity.getCreatedAt()
            );
        }
    }
