package com.online.library.controllers.api;

import com.online.library.domain.dto.BookDto;
import com.online.library.services.BookService;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

        private MockMvc mockMvc;

        @Mock
        private BookService bookService;

        @InjectMocks
        private BookController underTest;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.standaloneSetup(underTest)
                                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                                .build();
        }

        @Test
        void testCreateBookReturns201Created() throws Exception {
                // Given
                BookDto bookDto = BookDto.builder()
                                .id(1L)
                                .title("Test Book")
                                .publishYear(2021)
                                .isbn("123-456-789")
                                .build();

                when(bookService.save(any(BookDto.class))).thenReturn(bookDto);

                String bookJson = """
                                {"title":"Test Book","publishYear":2021,"isbn":"123-456-789"}
                                """;

                // When/Then
                mockMvc.perform(post("/api/v1/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bookJson))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.title").value("Test Book"))
                                .andExpect(jsonPath("$.publishYear").value(2021))
                                .andExpect(jsonPath("$.isbn").value("123-456-789"));

                verify(bookService).save(any(BookDto.class));
        }

        @Test
        void testListBooksReturnsPage() throws Exception {
                // Given
                BookDto book1 = BookDto.builder().id(1L).title("Book One").build();
                BookDto book2 = BookDto.builder().id(2L).title("Book Two").build();
                BookDto book3 = BookDto.builder().id(3L).title("Book Three").build();
                Page<BookDto> bookPage = new PageImpl<>(List.of(book1, book2, book3), PageRequest.of(0, 10), 3);

                when(bookService.findAll(any())).thenReturn(bookPage);

                // When/Then
                mockMvc.perform(get("/api/v1/books")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content.length()").value(3))
                                .andExpect(jsonPath("$.content[0].title").value("Book One"))
                                .andExpect(jsonPath("$.content[1].title").value("Book Two"))
                                .andExpect(jsonPath("$.content[2].title").value("Book Three"));

                verify(bookService).findAll(any());
        }

        @Test
        void testSearchBooksReturnsPage() throws Exception {
                // Given
                BookDto book = BookDto.builder().id(1L).title("Searched Book").build();
                Page<BookDto> searchPage = new PageImpl<>(List.of(book), PageRequest.of(0, 10), 1);

                when(bookService.searchBooks(eq("Searched"), any())).thenReturn(searchPage);

                // When/Then
                mockMvc.perform(get("/api/v1/books/search")
                                .param("q", "Searched")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content.length()").value(1))
                                .andExpect(jsonPath("$.content[0].title").value("Searched Book"));

                verify(bookService).searchBooks(eq("Searched"), any());
        }

        @Test
        void testSearchBooksWithNullSearchTerm() throws Exception {
                // Given
                Page<BookDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

                when(bookService.searchBooks(isNull(), any())).thenReturn(emptyPage);

                // When/Then
                mockMvc.perform(get("/api/v1/books/search")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content.length()").value(0));

                verify(bookService).searchBooks(isNull(), any());
        }

        @Test
        void testGetPopularBooksReturnsPage() throws Exception {
                // Given
                BookDto book1 = BookDto.builder().id(1L).title("Popular Book 1").build();
                BookDto book2 = BookDto.builder().id(2L).title("Popular Book 2").build();
                Page<BookDto> popularPage = new PageImpl<>(List.of(book1, book2), PageRequest.of(0, 10), 2);

                when(bookService.getPopularBooks(any())).thenReturn(popularPage);

                // When/Then
                mockMvc.perform(get("/api/v1/books/popular")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content.length()").value(2))
                                .andExpect(jsonPath("$.content[0].title").value("Popular Book 1"));

                verify(bookService).getPopularBooks(any());
        }

        @Test
        void testGetBookByIdReturns200WhenFound() throws Exception {
                // Given
                BookDto bookDto = BookDto.builder()
                                .id(1L)
                                .title("Test Book")
                                .isbn("123-456-789")
                                .build();

                when(bookService.findById(1L)).thenReturn(Optional.of(bookDto));

                // When/Then
                mockMvc.perform(get("/api/v1/books/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.title").value("Test Book"));

                verify(bookService).findById(1L);
        }

        @Test
        void testGetBookByIdReturns404WhenNotFound() throws Exception {
                // Given
                when(bookService.findById(999L)).thenReturn(Optional.empty());

                // When/Then
                mockMvc.perform(get("/api/v1/books/999")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());

                verify(bookService).findById(999L);
        }

        @Test
        void testFullUpdateBookReturns200WhenExists() throws Exception {
                // Given
                BookDto updatedDto = BookDto.builder()
                                .id(1L)
                                .title("Updated Book")
                                .isbn("999-888-777")
                                .publishYear(2021)
                                .build();

                when(bookService.isExists(1L)).thenReturn(true);
                when(bookService.save(any(BookDto.class))).thenReturn(updatedDto);

                String bookJson = """
                                {"title":"Updated Book","isbn":"999-888-777","publishYear":2021}
                                """;

                // When/Then
                mockMvc.perform(put("/api/v1/books/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bookJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Updated Book"))
                                .andExpect(jsonPath("$.isbn").value("999-888-777"));

                verify(bookService).isExists(1L);
                verify(bookService).save(any(BookDto.class));
        }

        @Test
        void testFullUpdateBookReturns404WhenNotExists() throws Exception {
                // Given
                when(bookService.isExists(999L)).thenReturn(false);

                String bookJson = """
                                {"title":"Test Book","isbn":"123-456-789","publishYear":2021}
                                """;

                // When/Then
                mockMvc.perform(put("/api/v1/books/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bookJson))
                                .andExpect(status().isNotFound());

                verify(bookService).isExists(999L);
                verify(bookService, never()).save(any());
        }

        @Test
        void testPartialUpdateBookReturns200WhenExists() throws Exception {
                // Given
                BookDto updatedDto = BookDto.builder()
                                .id(1L)
                                .title("Partial Updated Title")
                                .isbn("123-456-789")
                                .build();

                when(bookService.isExists(1L)).thenReturn(true);
                when(bookService.partialUpdate(eq(1L), any(BookDto.class))).thenReturn(updatedDto);

                String patchJson = """
                                {"title":"Partial Updated Title"}
                                """;

                // When/Then
                mockMvc.perform(patch("/api/v1/books/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(patchJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Partial Updated Title"));

                verify(bookService).isExists(1L);
                verify(bookService).partialUpdate(eq(1L), any(BookDto.class));
        }

        @Test
        void testPartialUpdateBookReturns404WhenNotExists() throws Exception {
                // Given
                when(bookService.isExists(999L)).thenReturn(false);

                String patchJson = """
                                {"title":"Updated Title"}
                                """;

                // When/Then
                mockMvc.perform(patch("/api/v1/books/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(patchJson))
                                .andExpect(status().isNotFound());

                verify(bookService).isExists(999L);
                verify(bookService, never()).partialUpdate(any(), any());
        }

        @Test
        void testDeleteBookReturns204() throws Exception {
                // Given
                doNothing().when(bookService).delete(1L);

                // When/Then
                mockMvc.perform(delete("/api/v1/books/1"))
                                .andExpect(status().isNoContent());

                verify(bookService).delete(1L);
        }

        @Test
        void testListBooksWithPagination() throws Exception {
                // Given
                BookDto book = BookDto.builder().id(1L).title("Book").build();
                Page<BookDto> bookPage = new PageImpl<>(List.of(book), PageRequest.of(2, 5), 11);

                when(bookService.findAll(any())).thenReturn(bookPage);

                // When/Then
                mockMvc.perform(get("/api/v1/books")
                                .param("page", "2")
                                .param("size", "5")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content.length()").value(1))
                                .andExpect(jsonPath("$.totalElements").value(11))
                                .andExpect(jsonPath("$.number").value(2));

                verify(bookService).findAll(any());
        }
}
