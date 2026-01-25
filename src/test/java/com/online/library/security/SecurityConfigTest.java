package com.online.library.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLoginPageIsAccessibleToAnonymous() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void testRegisterPageIsAccessibleToAnonymous() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }

    @Test
    void testApiGetBooksIsAccessibleToAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testApiGetAuthorsIsAccessibleToAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/authors")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testApiGetGenresIsAccessibleToAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/genres")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testDashboardRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testAdminPanelRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testAdminPanelDeniedForUserRole() throws Exception {
        mockMvc.perform(get("/admin/dashboard")
                .with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void testSwaggerUiDeniedForUserRole() throws Exception {
        mockMvc.perform(get("/swagger-ui.html")
                .with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void testApiPostRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/books")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test\",\"isbn\":\"123\"}"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testApiPostDeniedForUserRole() throws Exception {
        mockMvc.perform(post("/api/v1/books")
                .with(user("user").roles("USER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test\",\"isbn\":\"123\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testApiPostAllowedForAdminRole() throws Exception {
        mockMvc.perform(post("/api/v1/authors")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"lastName\":\"Author\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void testApiPutDeniedForUserRole() throws Exception {
        mockMvc.perform(put("/api/v1/books/1")
                .with(user("user").roles("USER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated\",\"isbn\":\"123\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testApiDeleteDeniedForUserRole() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1")
                .with(user("user").roles("USER"))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void testApiDeleteAllowedForAdminRole() throws Exception {
        mockMvc.perform(delete("/api/v1/authors/999")
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        mockMvc.perform(post("/login")
                .with(csrf())
                .param("username", "invalid")
                .param("password", "invalid"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post("/logout")
                .with(user("testuser").roles("USER"))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test
    void testPostWithoutCsrfIsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/authors")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"lastName\":\"Author\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testPostWithCsrfIsAllowed() throws Exception {
        mockMvc.perform(post("/api/v1/authors")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"lastName\":\"Author\"}"))
                .andExpect(status().isCreated());
    }
}
