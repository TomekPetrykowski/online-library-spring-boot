package com.online.library.controllers.views;

import com.online.library.domain.dto.BookDto;
import com.online.library.services.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Test
    public void testHomePageLoads() throws Exception {
        Page<BookDto> emptyPage = new PageImpl<>(List.of());
        when(bookService.findAll(any(Pageable.class))).thenReturn(emptyPage);
        when(bookService.getPopularBooks(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attributeExists("popularBooks"));
    }

    @Test
    public void testHomePageWithBooks() throws Exception {
        BookDto book1 = BookDto.builder().id(1L).title("Test Book 1").build();
        BookDto book2 = BookDto.builder().id(2L).title("Test Book 2").build();
        Page<BookDto> booksPage = new PageImpl<>(List.of(book1, book2));
        Page<BookDto> popularPage = new PageImpl<>(List.of(book1));

        when(bookService.findAll(any(Pageable.class))).thenReturn(booksPage);
        when(bookService.getPopularBooks(any(Pageable.class))).thenReturn(popularPage);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("books", booksPage))
                .andExpect(model().attribute("popularBooks", popularPage));
    }

    @Test
    public void testHomePageWithSearchTerm() throws Exception {
        BookDto book = BookDto.builder().id(1L).title("Searched Book").build();
        Page<BookDto> searchResults = new PageImpl<>(List.of(book));
        Page<BookDto> popularPage = new PageImpl<>(List.of());

        when(bookService.searchBooks(anyString(), any(Pageable.class))).thenReturn(searchResults);
        when(bookService.getPopularBooks(any(Pageable.class))).thenReturn(popularPage);

        mockMvc.perform(get("/").param("q", "Searched"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("searchTerm", "Searched"))
                .andExpect(model().attribute("books", searchResults));
    }

    @Test
    public void testHomePageWithSorting() throws Exception {
        Page<BookDto> emptyPage = new PageImpl<>(List.of());
        when(bookService.findAll(any(Pageable.class))).thenReturn(emptyPage);
        when(bookService.getPopularBooks(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/")
                .param("sort", "publishYear")
                .param("dir", "desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("sortField", "publishYear"))
                .andExpect(model().attribute("sortDir", "desc"));
    }
}
