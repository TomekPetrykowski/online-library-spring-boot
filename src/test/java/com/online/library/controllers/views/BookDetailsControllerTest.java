package com.online.library.controllers.views;

import com.online.library.domain.dto.*;
import com.online.library.domain.enums.ReservationStatus;
import com.online.library.services.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookDetailsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private RatingService ratingService;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private UserService userService;

    // ==================== Main Page Redirect Test ====================

    @Test
    void testMainPageRedirectsToHome() throws Exception {
        mockMvc.perform(get("/books/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    // ==================== Book Details Tests ====================

    @Test
    void testBookDetailsLoadsForAnonymousUser() throws Exception {
        BookDto book = BookDto.builder()
                .id(1L)
                .title("Test Book")
                .authors(new HashSet<>())
                .genres(new HashSet<>())
                .build();

        Page<CommentDto> commentsPage = new PageImpl<>(List.of());

        when(bookService.findById(1L)).thenReturn(Optional.of(book));
        when(ratingService.calculateAverageRating(1L)).thenReturn(BigDecimal.valueOf(4.5));
        when(ratingService.countRatingsForBook(1L)).thenReturn(10L);
        when(commentService.findByBookId(eq(1L), any(Pageable.class))).thenReturn(commentsPage);
        when(commentService.countCommentsForBook(1L)).thenReturn(5L);
        when(reservationService.hasAvailableCopies(1L)).thenReturn(true);

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-details"))
                .andExpect(model().attribute("book", book))
                .andExpect(model().attribute("averageRating", BigDecimal.valueOf(4.5)))
                .andExpect(model().attribute("ratingCount", 10L))
                .andExpect(model().attribute("commentCount", 5L))
                .andExpect(model().attribute("hasAvailableCopies", true));

        verify(bookService).findById(1L);
        verify(ratingService).calculateAverageRating(1L);
        verify(ratingService).countRatingsForBook(1L);
        verify(commentService).findByBookId(eq(1L), any(Pageable.class));
        verify(commentService).countCommentsForBook(1L);
        verify(reservationService).hasAvailableCopies(1L);
    }

    @Test
    void testBookDetailsLoadsForAuthenticatedUser() throws Exception {
        BookDto book = BookDto.builder()
                .id(1L)
                .title("Test Book")
                .authors(new HashSet<>())
                .genres(new HashSet<>())
                .build();

        UserResponseDto currentUser = UserResponseDto.builder()
                .id(1L)
                .username("testuser")
                .build();

        Page<CommentDto> commentsPage = new PageImpl<>(List.of());

        when(bookService.findById(1L)).thenReturn(Optional.of(book));
        when(ratingService.calculateAverageRating(1L)).thenReturn(BigDecimal.valueOf(4.0));
        when(ratingService.countRatingsForBook(1L)).thenReturn(5L);
        when(commentService.findByBookId(eq(1L), any(Pageable.class))).thenReturn(commentsPage);
        when(commentService.countCommentsForBook(1L)).thenReturn(3L);
        when(reservationService.hasAvailableCopies(1L)).thenReturn(true);
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
        when(ratingService.getUserRatingForBook(1L, 1L)).thenReturn(5);
        when(reservationService.canUserReserveBook(1L, 1L)).thenReturn(true);
        when(reservationService.getActiveReservation(1L, 1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/books/1")
                .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("book-details"))
                .andExpect(model().attribute("currentUser", currentUser))
                .andExpect(model().attribute("userRating", 5))
                .andExpect(model().attribute("canReserve", true));

        verify(userService).findByUsername("testuser");
        verify(ratingService).getUserRatingForBook(1L, 1L);
        verify(reservationService).canUserReserveBook(1L, 1L);
    }

    @Test
    void testBookDetailsWithActiveReservation() throws Exception {
        BookDto book = BookDto.builder()
                .id(1L)
                .title("Test Book")
                .authors(new HashSet<>())
                .genres(new HashSet<>())
                .build();

        UserResponseDto currentUser = UserResponseDto.builder()
                .id(1L)
                .username("testuser")
                .build();

        ReservationDto activeReservation = ReservationDto.builder()
                .id(1L)
                .status(ReservationStatus.OCZEKUJĄCA)
                .build();

        Page<CommentDto> commentsPage = new PageImpl<>(List.of());

        when(bookService.findById(1L)).thenReturn(Optional.of(book));
        when(ratingService.calculateAverageRating(1L)).thenReturn(BigDecimal.ZERO);
        when(ratingService.countRatingsForBook(1L)).thenReturn(0L);
        when(commentService.findByBookId(eq(1L), any(Pageable.class))).thenReturn(commentsPage);
        when(commentService.countCommentsForBook(1L)).thenReturn(0L);
        when(reservationService.hasAvailableCopies(1L)).thenReturn(false);
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
        when(ratingService.getUserRatingForBook(1L, 1L)).thenReturn(null);
        when(reservationService.canUserReserveBook(1L, 1L)).thenReturn(false);
        when(reservationService.getActiveReservation(1L, 1L)).thenReturn(Optional.of(activeReservation));

        mockMvc.perform(get("/books/1")
                .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("book-details"))
                .andExpect(model().attribute("activeReservation", activeReservation))
                .andExpect(model().attribute("hasAvailableCopies", false))
                .andExpect(model().attribute("canReserve", false));

        verify(reservationService).getActiveReservation(1L, 1L);
    }

    @Test
    void testBookDetailsBookNotFound() throws Exception {
        when(bookService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/books/999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));

        verify(bookService).findById(999L);
    }

    @Test
    void testBookDetailsWithCommentPagination() throws Exception {
        BookDto book = BookDto.builder()
                .id(1L)
                .title("Test Book")
                .authors(new HashSet<>())
                .genres(new HashSet<>())
                .build();

        Page<CommentDto> commentsPage = new PageImpl<>(List.of());

        when(bookService.findById(1L)).thenReturn(Optional.of(book));
        when(ratingService.calculateAverageRating(1L)).thenReturn(BigDecimal.ZERO);
        when(ratingService.countRatingsForBook(1L)).thenReturn(0L);
        when(commentService.findByBookId(eq(1L), any(Pageable.class))).thenReturn(commentsPage);
        when(commentService.countCommentsForBook(1L)).thenReturn(0L);
        when(reservationService.hasAvailableCopies(1L)).thenReturn(true);

        mockMvc.perform(get("/books/1")
                .param("commentPage", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-details"));

        verify(commentService).findByBookId(eq(1L), any(Pageable.class));
    }

    @Test
    void testBookDetailsWhenUserNotFoundInDatabase() throws Exception {
        BookDto book = BookDto.builder()
                .id(1L)
                .title("Test Book")
                .authors(new HashSet<>())
                .genres(new HashSet<>())
                .build();

        Page<CommentDto> commentsPage = new PageImpl<>(List.of());

        when(bookService.findById(1L)).thenReturn(Optional.of(book));
        when(ratingService.calculateAverageRating(1L)).thenReturn(BigDecimal.ZERO);
        when(ratingService.countRatingsForBook(1L)).thenReturn(0L);
        when(commentService.findByBookId(eq(1L), any(Pageable.class))).thenReturn(commentsPage);
        when(commentService.countCommentsForBook(1L)).thenReturn(0L);
        when(reservationService.hasAvailableCopies(1L)).thenReturn(true);
        when(userService.findByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(get("/books/1")
                .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("book-details"))
                .andExpect(model().attributeDoesNotExist("currentUser"));

        verify(userService).findByUsername("testuser");
        verify(ratingService, never()).getUserRatingForBook(anyLong(), anyLong());
    }

    // ==================== Rate Book Tests ====================

    @Test
    void testRateBookSuccess() throws Exception {
        UserResponseDto currentUser = UserResponseDto.builder()
                .id(1L)
                .username("testuser")
                .build();

        RatingDto ratingDto = RatingDto.builder().id(1L).rating(5).build();

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
        when(ratingService.rateBook(1L, 1L, 5)).thenReturn(ratingDto);

        mockMvc.perform(post("/books/1/rate")
                .param("rating", "5")
                .with(user("testuser").roles("USER"))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("success"));

        verify(userService).findByUsername("testuser");
        verify(ratingService).rateBook(1L, 1L, 5);
    }

    @Test
    void testRateBookRedirectsToLoginWhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/books/1/rate")
                .param("rating", "5")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testRateBookWithInvalidRatingTooLow() throws Exception {
        mockMvc.perform(post("/books/1/rate")
                .param("rating", "0")
                .with(user("testuser").roles("USER"))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("error"));

        verify(ratingService, never()).rateBook(anyLong(), anyLong(), any());
    }

    @Test
    void testRateBookWithInvalidRatingTooHigh() throws Exception {
        mockMvc.perform(post("/books/1/rate")
                .param("rating", "6")
                .with(user("testuser").roles("USER"))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("error"));

        verify(ratingService, never()).rateBook(anyLong(), anyLong(), any());
    }

    @Test
    void testRateBookUserNotFound() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(post("/books/1/rate")
                .param("rating", "5")
                .with(user("testuser").roles("USER"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));

        verify(userService).findByUsername("testuser");
        verify(ratingService, never()).rateBook(anyLong(), anyLong(), any());
    }

    // ==================== Add Comment Tests ====================

    @Test
    void testAddCommentSuccess() throws Exception {
        UserResponseDto currentUser = UserResponseDto.builder()
                .id(1L)
                .username("testuser")
                .build();

        CommentDto commentDto = CommentDto.builder().id(1L).content("Great book!").build();

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
        when(commentService.addComment(1L, 1L, "Great book!")).thenReturn(commentDto);

        mockMvc.perform(post("/books/1/comment")
                .param("content", "Great book!")
                .with(user("testuser").roles("USER"))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("success"));

        verify(userService).findByUsername("testuser");
        verify(commentService).addComment(1L, 1L, "Great book!");
    }

    @Test
    void testAddCommentRedirectsToLoginWhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/books/1/comment")
                .param("content", "Great book!")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testAddCommentWithEmptyContent() throws Exception {
        mockMvc.perform(post("/books/1/comment")
                .param("content", "")
                .with(user("testuser").roles("USER"))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("error"));

        verify(commentService, never()).addComment(anyLong(), anyLong(), any());
    }

    @Test
    void testAddCommentWithWhitespaceContent() throws Exception {
        mockMvc.perform(post("/books/1/comment")
                .param("content", "   ")
                .with(user("testuser").roles("USER"))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("error"));

        verify(commentService, never()).addComment(anyLong(), anyLong(), any());
    }

    @Test
    void testAddCommentUserNotFound() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(post("/books/1/comment")
                .param("content", "Great book!")
                .with(user("testuser").roles("USER"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));

        verify(userService).findByUsername("testuser");
        verify(commentService, never()).addComment(anyLong(), anyLong(), any());
    }

    @Test
    void testAddCommentContentIsTrimmed() throws Exception {
        UserResponseDto currentUser = UserResponseDto.builder()
                .id(1L)
                .username("testuser")
                .build();

        CommentDto commentDto = CommentDto.builder().id(1L).content("Trimmed content").build();

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
        when(commentService.addComment(1L, 1L, "Trimmed content")).thenReturn(commentDto);

        mockMvc.perform(post("/books/1/comment")
                .param("content", "  Trimmed content  ")
                .with(user("testuser").roles("USER"))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"));

        verify(commentService).addComment(1L, 1L, "Trimmed content");
    }

    // ==================== Reserve Book Tests ====================

    @Test
    void testReserveBookSuccess() throws Exception {
        UserResponseDto currentUser = UserResponseDto.builder()
                .id(1L)
                .username("testuser")
                .build();

        ReservationDto reservation = ReservationDto.builder()
                .id(1L)
                .status(ReservationStatus.OCZEKUJĄCA)
                .build();

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
        when(reservationService.createReservation(1L, 1L)).thenReturn(reservation);

        mockMvc.perform(post("/books/1/reserve")
                .with(user("testuser").roles("USER"))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("success"));

        verify(userService).findByUsername("testuser");
        verify(reservationService).createReservation(1L, 1L);
    }

    @Test
    void testReserveBookRedirectsToLoginWhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/books/1/reserve")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testReserveBookUserNotFound() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(post("/books/1/reserve")
                .with(user("testuser").roles("USER"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));

        verify(userService).findByUsername("testuser");
        verify(reservationService, never()).createReservation(anyLong(), anyLong());
    }

    @Test
    void testReserveBookNoCopiesAvailable() throws Exception {
        UserResponseDto currentUser = UserResponseDto.builder()
                .id(1L)
                .username("testuser")
                .build();

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
        when(reservationService.createReservation(1L, 1L))
                .thenThrow(new IllegalStateException("Brak dostępnych egzemplarzy"));

        mockMvc.perform(post("/books/1/reserve")
                .with(user("testuser").roles("USER"))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("error"));

        verify(reservationService).createReservation(1L, 1L);
    }

    @Test
    void testReserveBookUserAlreadyHasReservation() throws Exception {
        UserResponseDto currentUser = UserResponseDto.builder()
                .id(1L)
                .username("testuser")
                .build();

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
        when(reservationService.createReservation(1L, 1L))
                .thenThrow(new IllegalStateException("Użytkownik ma już aktywną rezerwację"));

        mockMvc.perform(post("/books/1/reserve")
                .with(user("testuser").roles("USER"))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"))
                .andExpect(flash().attributeExists("error"));

        verify(reservationService).createReservation(1L, 1L);
    }
}
