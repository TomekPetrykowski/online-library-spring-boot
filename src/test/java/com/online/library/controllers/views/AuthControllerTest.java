package com.online.library.controllers.views;

import com.online.library.domain.dto.UserResponseDto;
import com.online.library.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void testLoginPageLoads() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void testLoginPageWithErrorParameter() throws Exception {
        mockMvc.perform(get("/login")
                .param("error", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Nieprawidłowa nazwa użytkownika lub hasło"));
    }

    @Test
    void testLoginPageWithLogoutParameter() throws Exception {
        mockMvc.perform(get("/login")
                .param("logout", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("message", "Wylogowano pomyślnie"));
    }

    @Test
    void testLoginPageWithBothErrorAndLogout() throws Exception {
        mockMvc.perform(get("/login")
                .param("error", "")
                .param("logout", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Nieprawidłowa nazwa użytkownika lub hasło"))
                .andExpect(model().attribute("message", "Wylogowano pomyślnie"));
    }

    @Test
    void testRegisterFormLoads() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void testSuccessfulRegistration() throws Exception {
        when(userService.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userService.save(any())).thenReturn(UserResponseDto.builder().id(1L).username("newuser").build());

        mockMvc.perform(post("/register")
                .param("username", "newuser")
                .param("email", "newuser@example.com")
                .param("password", "password123456")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(userService).findByUsername("newuser");
        verify(userService).save(any());
    }

    @Test
    void testRegistrationFailsWhenUserExists() throws Exception {
        when(userService.findByUsername("existinguser"))
                .thenReturn(Optional.of(UserResponseDto.builder().username("existinguser").build()));

        mockMvc.perform(post("/register")
                .param("username", "existinguser")
                .param("email", "newuser@example.com")
                .param("password", "password123456")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors());

        verify(userService).findByUsername("existinguser");
        verify(userService, never()).save(any());
    }

    @Test
    void testRegistrationWithValidationErrors() throws Exception {
        when(userService.findByUsername(anyString())).thenReturn(Optional.empty());

        // Empty username should trigger validation error
        mockMvc.perform(post("/register")
                .param("username", "")
                .param("email", "invalid-email")
                .param("password", "short")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors());

        verify(userService, never()).save(any());
    }

    @Test
    void testRegistrationWithMissingEmail() throws Exception {
        when(userService.findByUsername(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/register")
                .param("username", "validuser")
                .param("email", "")
                .param("password", "password123456")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors());

        verify(userService, never()).save(any());
    }

    @Test
    void testRegistrationWithShortPassword() throws Exception {
        when(userService.findByUsername(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/register")
                .param("username", "validuser")
                .param("email", "valid@example.com")
                .param("password", "short")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors());

        verify(userService, never()).save(any());
    }

    @Test
    void testRegistrationSetsDefaultRoleAndEnabled() throws Exception {
        when(userService.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userService.save(any())).thenReturn(UserResponseDto.builder().id(1L).username("newuser").build());

        mockMvc.perform(post("/register")
                .param("username", "newuser")
                .param("email", "newuser@example.com")
                .param("password", "password123456")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(userService).save(argThat(user -> user.getRole() == com.online.library.domain.enums.UserRole.USER &&
                user.getEnabled() == true));
    }
}
