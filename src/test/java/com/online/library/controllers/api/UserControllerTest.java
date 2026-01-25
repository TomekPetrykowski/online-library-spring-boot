package com.online.library.controllers.api;

import com.online.library.domain.dto.UserRequestDto;
import com.online.library.domain.dto.UserResponseDto;
import com.online.library.domain.enums.UserRole;
import com.online.library.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController underTest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(underTest)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void testCreateUserReturns201Created() throws Exception {
        // Given
        UserResponseDto userResponseDto = UserResponseDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        when(userService.save(any(UserRequestDto.class))).thenReturn(userResponseDto);

        String userJson = """
                {"username":"testuser","password":"securePassword123","email":"test@example.com"}
                """;

        // When/Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).save(any(UserRequestDto.class));
    }

    @Test
    void testListUsersReturnsPage() throws Exception {
        // Given
        UserResponseDto user1 = UserResponseDto.builder()
                .id(1L).username("user1").email("user1@example.com").role(UserRole.USER).build();
        UserResponseDto user2 = UserResponseDto.builder()
                .id(2L).username("admin1").email("admin@example.com").role(UserRole.ADMIN).build();
        Page<UserResponseDto> userPage = new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 10), 2);

        when(userService.findAll(any())).thenReturn(userPage);

        // When/Then
        mockMvc.perform(get("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].username").value("user1"))
                .andExpect(jsonPath("$.content[1].username").value("admin1"));

        verify(userService).findAll(any());
    }

    @Test
    void testGetUserByIdReturns200WhenFound() throws Exception {
        // Given
        UserResponseDto userDto = UserResponseDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        when(userService.findById(1L)).thenReturn(Optional.of(userDto));

        // When/Then
        mockMvc.perform(get("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).findById(1L);
    }

    @Test
    void testGetUserByIdReturns404WhenNotFound() throws Exception {
        // Given
        when(userService.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/v1/users/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService).findById(999L);
    }

    @Test
    void testFullUpdateUserReturns200WhenExists() throws Exception {
        // Given
        UserResponseDto updatedDto = UserResponseDto.builder()
                .id(1L)
                .username("updateduser")
                .email("updated@example.com")
                .role(UserRole.USER)
                .build();

        when(userService.isExists(1L)).thenReturn(true);
        when(userService.save(any(UserRequestDto.class))).thenReturn(updatedDto);

        String userJson = """
                {"username":"updateduser","password":"newPassword123456","email":"updated@example.com"}
                """;

        // When/Then
        mockMvc.perform(put("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService).isExists(1L);
        verify(userService).save(any(UserRequestDto.class));
    }

    @Test
    void testFullUpdateUserReturns404WhenNotExists() throws Exception {
        // Given
        when(userService.isExists(999L)).thenReturn(false);

        String userJson = """
                {"username":"testuser","password":"password123456","email":"test@example.com"}
                """;

        // When/Then
        mockMvc.perform(put("/api/v1/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isNotFound());

        verify(userService).isExists(999L);
        verify(userService, never()).save(any());
    }

    @Test
    void testPartialUpdateUserReturns200WhenExists() throws Exception {
        // Given
        UserResponseDto updatedDto = UserResponseDto.builder()
                .id(1L)
                .username("testuser")
                .email("newemail@example.com")
                .role(UserRole.USER)
                .build();

        when(userService.isExists(1L)).thenReturn(true);
        when(userService.partialUpdate(eq(1L), any(UserRequestDto.class))).thenReturn(updatedDto);

        String patchJson = """
                {"email":"newemail@example.com"}
                """;

        // When/Then
        mockMvc.perform(patch("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newemail@example.com"));

        verify(userService).isExists(1L);
        verify(userService).partialUpdate(eq(1L), any(UserRequestDto.class));
    }

    @Test
    void testPartialUpdateUserReturns404WhenNotExists() throws Exception {
        // Given
        when(userService.isExists(999L)).thenReturn(false);

        String patchJson = """
                {"email":"new@example.com"}
                """;

        // When/Then
        mockMvc.perform(patch("/api/v1/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isNotFound());

        verify(userService).isExists(999L);
        verify(userService, never()).partialUpdate(any(), any());
    }

    @Test
    void testDeleteUserReturns204() throws Exception {
        // Given
        doNothing().when(userService).delete(1L);

        // When/Then
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }

    @Test
    void testCreateUserWithAdminRole() throws Exception {
        // Given
        UserResponseDto userResponseDto = UserResponseDto.builder()
                .id(1L)
                .username("adminuser")
                .email("admin@example.com")
                .role(UserRole.ADMIN)
                .build();

        when(userService.save(any(UserRequestDto.class))).thenReturn(userResponseDto);

        String userJson = """
                {"username":"adminuser","password":"adminPassword123","email":"admin@example.com","role":"ADMIN"}
                """;

        // When/Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(userService).save(any(UserRequestDto.class));
    }
}
