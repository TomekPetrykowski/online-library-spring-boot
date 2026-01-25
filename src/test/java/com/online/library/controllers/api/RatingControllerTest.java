package com.online.library.controllers.api;

import com.online.library.domain.dto.BookDto;
import com.online.library.domain.dto.RatingDto;
import com.online.library.domain.dto.UserResponseDto;
import com.online.library.services.RatingService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RatingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RatingService ratingService;

    @InjectMocks
    private RatingController underTest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(underTest)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void testCreateRatingReturns201Created() throws Exception {
        // Given
        UserResponseDto user = UserResponseDto.builder().id(1L).username("testuser").build();
        BookDto book = BookDto.builder().id(1L).title("Test Book").build();
        RatingDto ratingDto = RatingDto.builder()
                .id(1L)
                .user(user)
                .book(book)
                .rating(5)
                .createdAt(LocalDateTime.now())
                .build();

        when(ratingService.save(any(RatingDto.class))).thenReturn(ratingDto);

        String ratingJson = """
                {"user":{"id":1},"book":{"id":1},"rating":5}
                """;

        // When/Then
        mockMvc.perform(post("/api/v1/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ratingJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(5));

        verify(ratingService).save(any(RatingDto.class));
    }

    @Test
    void testListRatingsReturnsPage() throws Exception {
        // Given
        RatingDto rating1 = RatingDto.builder().id(1L).rating(5).build();
        RatingDto rating2 = RatingDto.builder().id(2L).rating(4).build();
        RatingDto rating3 = RatingDto.builder().id(3L).rating(3).build();
        Page<RatingDto> ratingPage = new PageImpl<>(List.of(rating1, rating2, rating3), PageRequest.of(0, 10), 3);

        when(ratingService.findAll(any())).thenReturn(ratingPage);

        // When/Then
        mockMvc.perform(get("/api/v1/ratings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].rating").value(5))
                .andExpect(jsonPath("$.content[1].rating").value(4));

        verify(ratingService).findAll(any());
    }

    @Test
    void testGetRatingByIdReturns200WhenFound() throws Exception {
        // Given
        RatingDto ratingDto = RatingDto.builder()
                .id(1L)
                .rating(5)
                .createdAt(LocalDateTime.now())
                .build();

        when(ratingService.findById(1L)).thenReturn(Optional.of(ratingDto));

        // When/Then
        mockMvc.perform(get("/api/v1/ratings/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(5));

        verify(ratingService).findById(1L);
    }

    @Test
    void testGetRatingByIdReturns404WhenNotFound() throws Exception {
        // Given
        when(ratingService.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/v1/ratings/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(ratingService).findById(999L);
    }

    @Test
    void testFullUpdateRatingReturns200WhenExists() throws Exception {
        // Given
        RatingDto updatedDto = RatingDto.builder()
                .id(1L)
                .rating(4)
                .build();

        when(ratingService.isExists(1L)).thenReturn(true);
        when(ratingService.save(any(RatingDto.class))).thenReturn(updatedDto);

        String ratingJson = """
                {"user":{"id":1},"book":{"id":1},"rating":4}
                """;

        // When/Then
        mockMvc.perform(put("/api/v1/ratings/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ratingJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4));

        verify(ratingService).isExists(1L);
        verify(ratingService).save(any(RatingDto.class));
    }

    @Test
    void testFullUpdateRatingReturns404WhenNotExists() throws Exception {
        // Given
        when(ratingService.isExists(999L)).thenReturn(false);

        String ratingJson = """
                {"user":{"id":1},"book":{"id":1},"rating":3}
                """;

        // When/Then
        mockMvc.perform(put("/api/v1/ratings/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ratingJson))
                .andExpect(status().isNotFound());

        verify(ratingService).isExists(999L);
        verify(ratingService, never()).save(any());
    }

    @Test
    void testPartialUpdateRatingReturns200WhenExists() throws Exception {
        // Given
        RatingDto updatedDto = RatingDto.builder()
                .id(1L)
                .rating(3)
                .build();

        when(ratingService.isExists(1L)).thenReturn(true);
        when(ratingService.partialUpdate(eq(1L), any(RatingDto.class))).thenReturn(updatedDto);

        String patchJson = """
                {"rating":3}
                """;

        // When/Then
        mockMvc.perform(patch("/api/v1/ratings/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(3));

        verify(ratingService).isExists(1L);
        verify(ratingService).partialUpdate(eq(1L), any(RatingDto.class));
    }

    @Test
    void testPartialUpdateRatingReturns404WhenNotExists() throws Exception {
        // Given
        when(ratingService.isExists(999L)).thenReturn(false);

        String patchJson = """
                {"rating":2}
                """;

        // When/Then
        mockMvc.perform(patch("/api/v1/ratings/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isNotFound());

        verify(ratingService).isExists(999L);
        verify(ratingService, never()).partialUpdate(any(), any());
    }

    @Test
    void testDeleteRatingReturns204() throws Exception {
        // Given
        doNothing().when(ratingService).delete(1L);

        // When/Then
        mockMvc.perform(delete("/api/v1/ratings/1"))
                .andExpect(status().isNoContent());

        verify(ratingService).delete(1L);
    }

    @Test
    void testCreateRatingWithMinValue() throws Exception {
        // Given
        RatingDto ratingDto = RatingDto.builder()
                .id(1L)
                .rating(1)
                .build();

        when(ratingService.save(any(RatingDto.class))).thenReturn(ratingDto);

        String ratingJson = """
                {"user":{"id":1},"book":{"id":1},"rating":1}
                """;

        // When/Then
        mockMvc.perform(post("/api/v1/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ratingJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(1));

        verify(ratingService).save(any(RatingDto.class));
    }
}
