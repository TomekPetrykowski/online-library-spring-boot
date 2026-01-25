package com.online.library.controllers.api;

import com.online.library.domain.dto.GenreDto;
import com.online.library.services.GenreService;
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
class GenreControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GenreService genreService;

    @InjectMocks
    private GenreController underTest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(underTest)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void testCreateGenreReturns201Created() throws Exception {
        // Given
        GenreDto genreDto = GenreDto.builder()
                .id(1L)
                .name("Horror")
                .build();

        when(genreService.save(any(GenreDto.class))).thenReturn(genreDto);

        String genreJson = """
                {"name":"Horror"}
                """;

        // When/Then
        mockMvc.perform(post("/api/v1/genres")
                .contentType(MediaType.APPLICATION_JSON)
                .content(genreJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Horror"));

        verify(genreService).save(any(GenreDto.class));
    }

    @Test
    void testListGenresReturnsPage() throws Exception {
        // Given
        GenreDto genre1 = GenreDto.builder().id(1L).name("Horror").build();
        GenreDto genre2 = GenreDto.builder().id(2L).name("Fantasy").build();
        GenreDto genre3 = GenreDto.builder().id(3L).name("Sci-Fi").build();
        Page<GenreDto> genrePage = new PageImpl<>(List.of(genre1, genre2, genre3), PageRequest.of(0, 10), 3);

        when(genreService.findAll(any())).thenReturn(genrePage);

        // When/Then
        mockMvc.perform(get("/api/v1/genres")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].name").value("Horror"))
                .andExpect(jsonPath("$.content[1].name").value("Fantasy"));

        verify(genreService).findAll(any());
    }

    @Test
    void testGetGenreByIdReturns200WhenFound() throws Exception {
        // Given
        GenreDto genreDto = GenreDto.builder()
                .id(1L)
                .name("Horror")
                .build();

        when(genreService.findById(1L)).thenReturn(Optional.of(genreDto));

        // When/Then
        mockMvc.perform(get("/api/v1/genres/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Horror"));

        verify(genreService).findById(1L);
    }

    @Test
    void testGetGenreByIdReturns404WhenNotFound() throws Exception {
        // Given
        when(genreService.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/v1/genres/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(genreService).findById(999L);
    }

    @Test
    void testFullUpdateGenreReturns200WhenExists() throws Exception {
        // Given
        GenreDto genreDto = GenreDto.builder()
                .id(1L)
                .name("Updated Genre")
                .build();

        when(genreService.isExists(1L)).thenReturn(true);
        when(genreService.save(any(GenreDto.class))).thenReturn(genreDto);

        String genreJson = """
                {"name":"Updated Genre"}
                """;

        // When/Then
        mockMvc.perform(put("/api/v1/genres/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(genreJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Genre"));

        verify(genreService).isExists(1L);
        verify(genreService).save(any(GenreDto.class));
    }

    @Test
    void testFullUpdateGenreReturns404WhenNotExists() throws Exception {
        // Given
        when(genreService.isExists(999L)).thenReturn(false);

        String genreJson = """
                {"name":"Test Genre"}
                """;

        // When/Then
        mockMvc.perform(put("/api/v1/genres/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(genreJson))
                .andExpect(status().isNotFound());

        verify(genreService).isExists(999L);
        verify(genreService, never()).save(any());
    }

    @Test
    void testPartialUpdateGenreReturns200WhenExists() throws Exception {
        // Given
        GenreDto updatedDto = GenreDto.builder()
                .id(1L)
                .name("Partial Updated")
                .build();

        when(genreService.isExists(1L)).thenReturn(true);
        when(genreService.partialUpdate(eq(1L), any(GenreDto.class))).thenReturn(updatedDto);

        String patchJson = """
                {"name":"Partial Updated"}
                """;

        // When/Then
        mockMvc.perform(patch("/api/v1/genres/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Partial Updated"));

        verify(genreService).isExists(1L);
        verify(genreService).partialUpdate(eq(1L), any(GenreDto.class));
    }

    @Test
    void testPartialUpdateGenreReturns404WhenNotExists() throws Exception {
        // Given
        when(genreService.isExists(999L)).thenReturn(false);

        String patchJson = """
                {"name":"Updated"}
                """;

        // When/Then
        mockMvc.perform(patch("/api/v1/genres/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isNotFound());

        verify(genreService).isExists(999L);
        verify(genreService, never()).partialUpdate(any(), any());
    }

    @Test
    void testDeleteGenreReturns204() throws Exception {
        // Given
        doNothing().when(genreService).delete(1L);

        // When/Then
        mockMvc.perform(delete("/api/v1/genres/1"))
                .andExpect(status().isNoContent());

        verify(genreService).delete(1L);
    }
}
