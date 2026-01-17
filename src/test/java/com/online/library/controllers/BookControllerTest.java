package com.online.library.controllers;

import com.online.library.domain.dto.BookDto;
import com.online.library.services.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController underTest;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(underTest).build();
    }

    @Test
    public void testThatCreateBookReturnsHttpStatus201Created() throws Exception {
        BookDto bookDto = BookDto.builder()
                .id(1L)
                .title("Test Book")
                .publishYear(2021)
                .isbn("123-456-789")
                .build();
        when(bookService.save(any(BookDto.class))).thenReturn(bookDto);

        String bookJson = "{\"id\":1,\"title\":\"Test Book\",\"publishYear\":2021,\"isbn\":\"123-456-789\"}";

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson)
        ).andExpect(
                MockMvcResultMatchers.status().isCreated()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").value(1)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value("Test Book")
        );
    }

    @Test
    public void testThatGetBookReturnsHttpStatus200WhenBookExists() throws Exception {
        BookDto bookDto = BookDto.builder().id(1L).title("Test Book").build();
        when(bookService.findById(eq(1L))).thenReturn(Optional.of(bookDto));

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").value(1)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value("Test Book")
        );
    }
}
