package com.online.library.controllers.api;

import com.online.library.domain.dto.AuthorDto;
import com.online.library.services.AuthorService;
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
class AuthorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthorService authorService;

    @InjectMocks
    private AuthorController underTest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(underTest)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void testCreateAuthorReturns201Created() throws Exception {
        // Given
        AuthorDto authorDto = AuthorDto.builder()
                .id(1L)
                .name("H.P.")
                .lastName("Lovecraft")
                .bio("American writer")
                .build();

        when(authorService.save(any(AuthorDto.class))).thenReturn(authorDto);

        String authorJson = """
                {"name":"H.P.","lastName":"Lovecraft","bio":"American writer"}
                """;

        // When/Then
        mockMvc.perform(post("/api/v1/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authorJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("H.P."))
                .andExpect(jsonPath("$.lastName").value("Lovecraft"));

        verify(authorService).save(any(AuthorDto.class));
    }

    @Test
    void testListAuthorsReturnsPage() throws Exception {
        // Given
        AuthorDto author1 = AuthorDto.builder().id(1L).name("H.P.").lastName("Lovecraft").build();
        AuthorDto author2 = AuthorDto.builder().id(2L).name("Stephen").lastName("King").build();
        Page<AuthorDto> authorPage = new PageImpl<>(List.of(author1, author2), PageRequest.of(0, 10), 2);

        when(authorService.findAll(any())).thenReturn(authorPage);

        // When/Then
        mockMvc.perform(get("/api/v1/authors")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("H.P."))
                .andExpect(jsonPath("$.content[1].name").value("Stephen"));

        verify(authorService).findAll(any());
    }

    @Test
    void testGetAuthorByIdReturns200WhenFound() throws Exception {
        // Given
        AuthorDto authorDto = AuthorDto.builder()
                .id(1L)
                .name("H.P.")
                .lastName("Lovecraft")
                .build();

        when(authorService.findById(1L)).thenReturn(Optional.of(authorDto));

        // When/Then
        mockMvc.perform(get("/api/v1/authors/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("H.P."));

        verify(authorService).findById(1L);
    }

    @Test
    void testGetAuthorByIdReturns404WhenNotFound() throws Exception {
        // Given
        when(authorService.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/v1/authors/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(authorService).findById(999L);
    }

    @Test
    void testFullUpdateAuthorReturns200WhenExists() throws Exception {
        // Given
        AuthorDto authorDto = AuthorDto.builder()
                .id(1L)
                .name("Updated")
                .lastName("Author")
                .build();

        when(authorService.isExists(1L)).thenReturn(true);
        when(authorService.save(any(AuthorDto.class))).thenReturn(authorDto);

        String authorJson = """
                {"name":"Updated","lastName":"Author"}
                """;

        // When/Then
        mockMvc.perform(put("/api/v1/authors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authorJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));

        verify(authorService).isExists(1L);
        verify(authorService).save(any(AuthorDto.class));
    }

    @Test
    void testFullUpdateAuthorReturns404WhenNotExists() throws Exception {
        // Given
        when(authorService.isExists(999L)).thenReturn(false);

        String authorJson = """
                {"name":"Test","lastName":"Author"}
                """;

        // When/Then
        mockMvc.perform(put("/api/v1/authors/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authorJson))
                .andExpect(status().isNotFound());

        verify(authorService).isExists(999L);
        verify(authorService, never()).save(any());
    }

    @Test
    void testPartialUpdateAuthorReturns200WhenExists() throws Exception {
        // Given
        AuthorDto updatedDto = AuthorDto.builder()
                .id(1L)
                .name("Updated Name")
                .lastName("Lovecraft")
                .build();

        when(authorService.isExists(1L)).thenReturn(true);
        when(authorService.partialUpdate(eq(1L), any(AuthorDto.class))).thenReturn(updatedDto);

        String patchJson = """
                {"name":"Updated Name"}
                """;

        // When/Then
        mockMvc.perform(patch("/api/v1/authors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(authorService).isExists(1L);
        verify(authorService).partialUpdate(eq(1L), any(AuthorDto.class));
    }

    @Test
    void testPartialUpdateAuthorReturns404WhenNotExists() throws Exception {
        // Given
        when(authorService.isExists(999L)).thenReturn(false);

        String patchJson = """
                {"name":"Updated Name"}
                """;

        // When/Then
        mockMvc.perform(patch("/api/v1/authors/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isNotFound());

        verify(authorService).isExists(999L);
        verify(authorService, never()).partialUpdate(any(), any());
    }

    @Test
    void testDeleteAuthorReturns204() throws Exception {
        // Given
        doNothing().when(authorService).delete(1L);

        // When/Then
        mockMvc.perform(delete("/api/v1/authors/1"))
                .andExpect(status().isNoContent());

        verify(authorService).delete(1L);
    }
}
