    package com.user_service.controller;

    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.user_service.dto.UserRequestDto;
    import com.user_service.dto.UserResponseDto;
    import com.user_service.exception.UserNotFoundException;
    import com.user_service.service.UserService;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
    import org.springframework.boot.test.mock.mockito.MockBean;
    import org.springframework.http.MediaType;

    import org.springframework.test.context.ActiveProfiles;
    import org.springframework.test.web.servlet.MockMvc;

    import java.time.LocalDateTime;
    import java.util.Arrays;
    import java.util.List;

    import static org.mockito.ArgumentMatchers.any;

    import static org.mockito.Mockito.*;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
    import static org.hamcrest.Matchers.containsString;


    @WebMvcTest(UserController.class)
    @ActiveProfiles("test")
    public class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private UserService userService;

        @Test
        void createdUser_ShouldReturnCreated() throws Exception {
            UserRequestDto request = new UserRequestDto("Test", "test@mail.com", 25);
            UserResponseDto response = new UserResponseDto(1L, "Test", "test@mail.com", 25, LocalDateTime.now());

            when(userService.createUser(any(UserRequestDto.class))).thenReturn(response);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Test"))
                    .andExpect(jsonPath("$.email").value("test@mail.com"))
                    .andExpect(jsonPath("$.age").value(25))
                    .andExpect(header().string("Location", containsString("/api/users/1")))
                    .andExpect(jsonPath("$._links.self.href").value(containsString("/api/users/1")));
        }

        @Test
        void getUserById_ShouldReturnUser() throws Exception {
            UserResponseDto response = new UserResponseDto(1L, "Test", "test@mail.com", 25, LocalDateTime.now());

            when(userService.findUserById(1L)).thenReturn(response);

            mockMvc.perform(get("/api/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Test"))
                    .andExpect(jsonPath("$._links.self.href").value(containsString("/api/users/1")))
                    .andExpect(jsonPath("$._links.all-users.href").value(containsString("/api/users")));
        }

        @Test
        void getUserById_ShouldReturnNotFound() throws Exception {
            when(userService.findUserById(999L)).thenThrow(new UserNotFoundException(999L));

            mockMvc.perform(get("/api/users/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void deleteUser_ShouldReturnNoContent() throws Exception {
            doNothing().when(userService).deleteUser(1L);

            mockMvc.perform(delete("/api/users/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void deleteUser_ShouldReturnNotFound() throws Exception {
            doThrow(new UserNotFoundException(999L)).when(userService).deleteUser(999L);

            mockMvc.perform(delete("/api/users/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void getAllUsers_ShouldReturnCollectionWithLinks() throws Exception {
            List<UserResponseDto> users = Arrays.asList(
                    new UserResponseDto(1L, "User1", "test1@mail.ru", 20, LocalDateTime.now()),
                    new UserResponseDto(2L, "User2", "test2@mail.ru", 30, LocalDateTime.now())
            );

            when(userService.findAllUsers()).thenReturn(users);

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.userResponseDtoList[0].id").value(1))
                    .andExpect(jsonPath("$._embedded.userResponseDtoList[0]._links.self.href")
                            .value(containsString("/api/users/1")))
                    .andExpect(jsonPath("$._embedded.userResponseDtoList[1]._links.self.href")
                            .value(containsString("/api/users/2")))
                    .andExpect(jsonPath("$._links.self.href").value(containsString("/api/users")));
        }
    }

