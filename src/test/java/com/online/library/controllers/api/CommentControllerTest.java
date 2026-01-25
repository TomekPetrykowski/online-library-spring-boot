package com.online.library.controllers.api;

import com.online.library.domain.dto.BookDto;
import com.online.library.domain.dto.CommentDto;
import com.online.library.domain.dto.UserResponseDto;
import com.online.library.services.CommentService;
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
class CommentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController underTest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(underTest)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void testCreateCommentReturns201Created() throws Exception {
        // Given
        UserResponseDto user = UserResponseDto.builder().id(1L).username("testuser").build();
        BookDto book = BookDto.builder().id(1L).title("Test Book").build();
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .user(user)
                .book(book)
                .content("Great book!")
                .createdAt(LocalDateTime.now())
                .build();

        when(commentService.save(any(CommentDto.class))).thenReturn(commentDto);

        String commentJson = """
                {"user":{"id":1},"book":{"id":1},"content":"Great book!"}
                """;

        // When/Then
        mockMvc.perform(post("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Great book!"));

        verify(commentService).save(any(CommentDto.class));
    }

    @Test
    void testListCommentsReturnsPage() throws Exception {
        // Given
        CommentDto comment1 = CommentDto.builder().id(1L).content("Comment 1").build();
        CommentDto comment2 = CommentDto.builder().id(2L).content("Comment 2").build();
        Page<CommentDto> commentPage = new PageImpl<>(List.of(comment1, comment2), PageRequest.of(0, 10), 2);

        when(commentService.findAll(any())).thenReturn(commentPage);

        // When/Then
        mockMvc.perform(get("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].content").value("Comment 1"));

        verify(commentService).findAll(any());
    }

    @Test
    void testGetCommentByIdReturns200WhenFound() throws Exception {
        // Given
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .content("Test comment")
                .createdAt(LocalDateTime.now())
                .build();

        when(commentService.findById(1L)).thenReturn(Optional.of(commentDto));

        // When/Then
        mockMvc.perform(get("/api/v1/comments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Test comment"));

        verify(commentService).findById(1L);
    }

    @Test
    void testGetCommentByIdReturns404WhenNotFound() throws Exception {
        // Given
        when(commentService.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/v1/comments/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(commentService).findById(999L);
    }

    @Test
    void testFullUpdateCommentReturns200WhenExists() throws Exception {
        // Given
        CommentDto updatedDto = CommentDto.builder()
                .id(1L)
                .content("Updated comment")
                .build();

        when(commentService.isExists(1L)).thenReturn(true);
        when(commentService.save(any(CommentDto.class))).thenReturn(updatedDto);

        String commentJson = """
                {"user":{"id":1},"book":{"id":1},"content":"Updated comment"}
                """;

        // When/Then
        mockMvc.perform(put("/api/v1/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated comment"));

        verify(commentService).isExists(1L);
        verify(commentService).save(any(CommentDto.class));
    }

    @Test
    void testFullUpdateCommentReturns404WhenNotExists() throws Exception {
        // Given
        when(commentService.isExists(999L)).thenReturn(false);

        String commentJson = """
                {"user":{"id":1},"book":{"id":1},"content":"Test"}
                """;

        // When/Then
        mockMvc.perform(put("/api/v1/comments/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentJson))
                .andExpect(status().isNotFound());

        verify(commentService).isExists(999L);
        verify(commentService, never()).save(any());
    }

    @Test
    void testPartialUpdateCommentReturns200WhenExists() throws Exception {
        // Given
        CommentDto updatedDto = CommentDto.builder()
                .id(1L)
                .content("Partial updated")
                .build();

        when(commentService.isExists(1L)).thenReturn(true);
        when(commentService.partialUpdate(eq(1L), any(CommentDto.class))).thenReturn(updatedDto);

        String patchJson = """
                {"content":"Partial updated"}
                """;

        // When/Then
        mockMvc.perform(patch("/api/v1/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Partial updated"));

        verify(commentService).isExists(1L);
        verify(commentService).partialUpdate(eq(1L), any(CommentDto.class));
    }

    @Test
    void testPartialUpdateCommentReturns404WhenNotExists() throws Exception {
        // Given
        when(commentService.isExists(999L)).thenReturn(false);

        String patchJson = """
                {"content":"Updated"}
                """;

        // When/Then
        mockMvc.perform(patch("/api/v1/comments/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isNotFound());

        verify(commentService).isExists(999L);
        verify(commentService, never()).partialUpdate(any(), any());
    }

    @Test
    void testDeleteCommentReturns204() throws Exception {
        // Given
        doNothing().when(commentService).delete(1L);

        // When/Then
        mockMvc.perform(delete("/api/v1/comments/1"))
                .andExpect(status().isNoContent());

        verify(commentService).delete(1L);
    }
}
