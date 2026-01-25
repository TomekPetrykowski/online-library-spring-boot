package com.online.library.controllers.views;

import com.online.library.domain.dto.*;
import com.online.library.domain.enums.ReservationStatus;
import com.online.library.domain.enums.UserRole;
import com.online.library.services.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private BookService bookService;

        @MockitoBean
        private AuthorService authorService;

        @MockitoBean
        private GenreService genreService;

        @MockitoBean
        private ReservationService reservationService;

        @MockitoBean
        private AnalyticsService analyticsService;

        @MockitoBean
        private ExportService exportService;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private FileStorageService fileStorageService;

        // ==================== Dashboard Tests ====================

        @Test
        void testAdminDashboardRequiresAuthentication() throws Exception {
                mockMvc.perform(get("/admin"))
                                .andExpect(status().is3xxRedirection());
        }

        @Test
        void testAdminDashboardAccessibleByAdmin() throws Exception {
                mockMvc.perform(get("/admin")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/dashboard"));
        }

        // ==================== Reports Tests ====================

        @Test
        void testReportsPageLoads() throws Exception {
                when(analyticsService.getMostPopularBooks(10)).thenReturn(List.of());
                when(analyticsService.getMostReadAuthors(10)).thenReturn(List.of());
                when(analyticsService.getMostActiveUsers(10)).thenReturn(List.of());

                mockMvc.perform(get("/admin/reports")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/reports"))
                                .andExpect(model().attributeExists("popularBooks"))
                                .andExpect(model().attributeExists("readAuthors"))
                                .andExpect(model().attributeExists("activeUsers"));

                verify(analyticsService).getMostPopularBooks(10);
                verify(analyticsService).getMostReadAuthors(10);
                verify(analyticsService).getMostActiveUsers(10);
        }

        @Test
        void testReportsPageWithData() throws Exception {
                BookStatDto bookStat = new BookStatDto();
                AuthorStatDto authorStat = new AuthorStatDto();
                UserStatDto userStat = new UserStatDto();

                when(analyticsService.getMostPopularBooks(10)).thenReturn(List.of(bookStat));
                when(analyticsService.getMostReadAuthors(10)).thenReturn(List.of(authorStat));
                when(analyticsService.getMostActiveUsers(10)).thenReturn(List.of(userStat));

                mockMvc.perform(get("/admin/reports")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(model().attribute("popularBooks", List.of(bookStat)))
                                .andExpect(model().attribute("readAuthors", List.of(authorStat)))
                                .andExpect(model().attribute("activeUsers", List.of(userStat)));
        }

        // ==================== Reservations Tests ====================

        @Test
        void testListReservations() throws Exception {
                Page<ReservationDto> reservationPage = new PageImpl<>(List.of());
                when(reservationService.findAll(any(Pageable.class))).thenReturn(reservationPage);

                mockMvc.perform(get("/admin/reservations")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/reservations"))
                                .andExpect(model().attributeExists("reservations"))
                                .andExpect(model().attributeExists("statuses"));

                verify(reservationService).findAll(any(Pageable.class));
        }

        @Test
        void testChangeReservationStatusSuccess() throws Exception {
                ReservationDto reservation = ReservationDto.builder()
                                .id(1L)
                                .status(ReservationStatus.POTWIERDZONA)
                                .build();
                when(reservationService.changeStatus(1L, ReservationStatus.POTWIERDZONA)).thenReturn(reservation);

                mockMvc.perform(post("/admin/reservations/1/status")
                                .param("status", "POTWIERDZONA")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/reservations"))
                                .andExpect(flash().attributeExists("success"));

                verify(reservationService).changeStatus(1L, ReservationStatus.POTWIERDZONA);
        }

        @Test
        void testChangeReservationStatusError() throws Exception {
                doThrow(new RuntimeException("Cannot change status"))
                                .when(reservationService).changeStatus(anyLong(), any());

                mockMvc.perform(post("/admin/reservations/1/status")
                                .param("status", "POTWIERDZONA")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/reservations"))
                                .andExpect(flash().attributeExists("error"));

                verify(reservationService).changeStatus(1L, ReservationStatus.POTWIERDZONA);
        }

        // ==================== Books Tests ====================

        @Test
        void testListBooks() throws Exception {
                Page<BookDto> bookPage = new PageImpl<>(List.of());
                when(bookService.findAll(any(Pageable.class))).thenReturn(bookPage);

                mockMvc.perform(get("/admin/books")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/books"))
                                .andExpect(model().attributeExists("books"));

                verify(bookService).findAll(any(Pageable.class));
        }

        @Test
        void testNewBookForm() throws Exception {
                Page<AuthorDto> authorPage = new PageImpl<>(List.of());
                Page<GenreDto> genrePage = new PageImpl<>(List.of());

                when(authorService.findAll(any(Pageable.class))).thenReturn(authorPage);
                when(genreService.findAll(any(Pageable.class))).thenReturn(genrePage);

                mockMvc.perform(get("/admin/books/new")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/book-form"))
                                .andExpect(model().attributeExists("book"))
                                .andExpect(model().attributeExists("allAuthors"))
                                .andExpect(model().attributeExists("allGenres"));

                verify(authorService).findAll(any(Pageable.class));
                verify(genreService).findAll(any(Pageable.class));
        }

        @Test
        void testCreateBookSuccess() throws Exception {
                Page<AuthorDto> authorPage = new PageImpl<>(List.of());
                Page<GenreDto> genrePage = new PageImpl<>(List.of());

                when(authorService.findAll(any(Pageable.class))).thenReturn(authorPage);
                when(genreService.findAll(any(Pageable.class))).thenReturn(genrePage);
                when(bookService.save(any(BookDto.class))).thenReturn(BookDto.builder().id(1L).build());

                mockMvc.perform(post("/admin/books")
                                .param("title", "New Book")
                                .param("isbn", "978-0-123456-78-9")
                                .param("publishYear", "2021")
                                .param("copiesAvailable", "5")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/books"))
                                .andExpect(flash().attributeExists("success"));

                verify(bookService).save(any(BookDto.class));
        }

        @Test
        void testCreateBookWithCoverImage() throws Exception {
                Page<AuthorDto> authorPage = new PageImpl<>(List.of());
                Page<GenreDto> genrePage = new PageImpl<>(List.of());

                when(authorService.findAll(any(Pageable.class))).thenReturn(authorPage);
                when(genreService.findAll(any(Pageable.class))).thenReturn(genrePage);
                when(bookService.save(any(BookDto.class))).thenReturn(BookDto.builder().id(1L).build());
                when(fileStorageService.storeFile(any(), eq("covers"))).thenReturn("covers/test.jpg");

                MockMultipartFile coverImage = new MockMultipartFile(
                                "coverImage", "test.jpg", "image/jpeg", "test image content".getBytes());

                mockMvc.perform(multipart("/admin/books")
                                .file(coverImage)
                                .param("title", "New Book")
                                .param("isbn", "978-0-123456-78-9")
                                .param("publishYear", "2021")
                                .param("copiesAvailable", "5")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/books"));

                verify(fileStorageService).storeFile(any(), eq("covers"));
                verify(bookService).save(any(BookDto.class));
        }

        @Test
        void testCreateBookWithAuthorsAndGenres() throws Exception {
                AuthorDto author = AuthorDto.builder().id(1L).name("Test").lastName("Author").build();
                GenreDto genre = GenreDto.builder().id(1L).name("Horror").build();

                Page<AuthorDto> authorPage = new PageImpl<>(List.of(author));
                Page<GenreDto> genrePage = new PageImpl<>(List.of(genre));

                when(authorService.findAll(any(Pageable.class))).thenReturn(authorPage);
                when(genreService.findAll(any(Pageable.class))).thenReturn(genrePage);
                when(authorService.findById(1L)).thenReturn(Optional.of(author));
                when(genreService.findById(1L)).thenReturn(Optional.of(genre));
                when(bookService.save(any(BookDto.class))).thenReturn(BookDto.builder().id(1L).build());

                mockMvc.perform(post("/admin/books")
                                .param("title", "New Book")
                                .param("isbn", "978-0-123456-78-9")
                                .param("publishYear", "2021")
                                .param("copiesAvailable", "5")
                                .param("authorIds", "1")
                                .param("genreIds", "1")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/books"));

                verify(authorService).findById(1L);
                verify(genreService).findById(1L);
                verify(bookService).save(any(BookDto.class));
        }

        @Test
        void testEditBookForm() throws Exception {
                BookDto book = BookDto.builder().id(1L).title("Test Book").build();
                Page<AuthorDto> authorPage = new PageImpl<>(List.of());
                Page<GenreDto> genrePage = new PageImpl<>(List.of());

                when(bookService.findById(1L)).thenReturn(Optional.of(book));
                when(authorService.findAll(any(Pageable.class))).thenReturn(authorPage);
                when(genreService.findAll(any(Pageable.class))).thenReturn(genrePage);

                mockMvc.perform(get("/admin/books/1/edit")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/book-form"))
                                .andExpect(model().attribute("book", book));

                verify(bookService).findById(1L);
        }

        @Test
        void testUpdateBook() throws Exception {
                BookDto existingBook = BookDto.builder().id(1L).title("Old Title").coverImagePath("covers/old.jpg")
                                .build();
                Page<AuthorDto> authorPage = new PageImpl<>(List.of());
                Page<GenreDto> genrePage = new PageImpl<>(List.of());

                when(bookService.findById(1L)).thenReturn(Optional.of(existingBook));
                when(authorService.findAll(any(Pageable.class))).thenReturn(authorPage);
                when(genreService.findAll(any(Pageable.class))).thenReturn(genrePage);
                when(bookService.save(any(BookDto.class))).thenReturn(existingBook);

                mockMvc.perform(post("/admin/books/1")
                                .param("title", "Updated Title")
                                .param("isbn", "978-0-123456-78-9")
                                .param("publishYear", "2021")
                                .param("copiesAvailable", "5")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/books"))
                                .andExpect(flash().attributeExists("success"));

                verify(bookService).save(any(BookDto.class));
        }

        @Test
        void testUpdateBookWithRemoveCover() throws Exception {
                BookDto existingBook = BookDto.builder().id(1L).coverImagePath("covers/old.jpg").build();
                Page<AuthorDto> authorPage = new PageImpl<>(List.of());
                Page<GenreDto> genrePage = new PageImpl<>(List.of());

                when(bookService.findById(1L)).thenReturn(Optional.of(existingBook));
                when(authorService.findAll(any(Pageable.class))).thenReturn(authorPage);
                when(genreService.findAll(any(Pageable.class))).thenReturn(genrePage);
                when(bookService.save(any(BookDto.class))).thenReturn(existingBook);

                mockMvc.perform(post("/admin/books/1")
                                .param("title", "Book Title")
                                .param("isbn", "978-0-123456-78-9")
                                .param("publishYear", "2021")
                                .param("copiesAvailable", "5")
                                .param("removeCover", "true")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/books"));

                verify(bookService).save(any(BookDto.class));
        }

        @Test
        void testDeleteBook() throws Exception {
                doNothing().when(bookService).delete(1L);

                mockMvc.perform(post("/admin/books/1/delete")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/books"))
                                .andExpect(flash().attributeExists("success"));

                verify(bookService).delete(1L);
        }

        // ==================== Authors Tests ====================

        @Test
        void testListAuthors() throws Exception {
                Page<AuthorDto> authorPage = new PageImpl<>(List.of());
                when(authorService.findAll(any(Pageable.class))).thenReturn(authorPage);

                mockMvc.perform(get("/admin/authors")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/authors"))
                                .andExpect(model().attributeExists("authors"));

                verify(authorService).findAll(any(Pageable.class));
        }

        @Test
        void testNewAuthorForm() throws Exception {
                mockMvc.perform(get("/admin/authors/new")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/author-form"))
                                .andExpect(model().attributeExists("author"));
        }

        @Test
        void testCreateAuthorSuccess() throws Exception {
                when(authorService.save(any(AuthorDto.class)))
                                .thenReturn(AuthorDto.builder().id(1L).name("Test").lastName("Author").build());

                mockMvc.perform(post("/admin/authors")
                                .param("name", "Test")
                                .param("lastName", "Author")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/authors"))
                                .andExpect(flash().attributeExists("success"));

                verify(authorService).save(any(AuthorDto.class));
        }

        @Test
        void testDeleteAuthor() throws Exception {
                doNothing().when(authorService).delete(1L);

                mockMvc.perform(post("/admin/authors/1/delete")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/authors"))
                                .andExpect(flash().attributeExists("success"));

                verify(authorService).delete(1L);
        }

        // ==================== Genres Tests ====================

        @Test
        void testListGenres() throws Exception {
                Page<GenreDto> genrePage = new PageImpl<>(List.of());
                when(genreService.findAll(any(Pageable.class))).thenReturn(genrePage);

                mockMvc.perform(get("/admin/genres")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/genres"))
                                .andExpect(model().attributeExists("genres"));

                verify(genreService).findAll(any(Pageable.class));
        }

        @Test
        void testNewGenreForm() throws Exception {
                mockMvc.perform(get("/admin/genres/new")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/genre-form"))
                                .andExpect(model().attributeExists("genre"));
        }

        @Test
        void testCreateGenreSuccess() throws Exception {
                when(genreService.save(any(GenreDto.class)))
                                .thenReturn(GenreDto.builder().id(1L).name("Horror").build());

                mockMvc.perform(post("/admin/genres")
                                .param("name", "Horror")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/genres"))
                                .andExpect(flash().attributeExists("success"));

                verify(genreService).save(any(GenreDto.class));
        }

        @Test
        void testDeleteGenre() throws Exception {
                doNothing().when(genreService).delete(1L);

                mockMvc.perform(post("/admin/genres/1/delete")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/genres"))
                                .andExpect(flash().attributeExists("success"));

                verify(genreService).delete(1L);
        }

        // ==================== Export Tests ====================

        @Test
        void testExportPopularBooksCsv() throws Exception {
                when(analyticsService.getMostPopularBooks(100)).thenReturn(List.of());
                when(exportService.exportPopularBooksToCsv(any())).thenReturn("csv content".getBytes());

                mockMvc.perform(get("/admin/reports/export/popular-books")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(header().string("Content-Disposition",
                                                "attachment; filename=popularne_ksiazki.csv"))
                                .andExpect(content().contentType("text/csv;charset=UTF-8"));

                verify(analyticsService).getMostPopularBooks(100);
                verify(exportService).exportPopularBooksToCsv(any());
        }

        @Test
        void testExportReadAuthorsCsv() throws Exception {
                when(analyticsService.getMostReadAuthors(100)).thenReturn(List.of());
                when(exportService.exportReadAuthorsToCsv(any())).thenReturn("csv content".getBytes());

                mockMvc.perform(get("/admin/reports/export/read-authors")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(header().string("Content-Disposition",
                                                "attachment; filename=czytani_autorzy.csv"))
                                .andExpect(content().contentType("text/csv;charset=UTF-8"));

                verify(analyticsService).getMostReadAuthors(100);
                verify(exportService).exportReadAuthorsToCsv(any());
        }

        @Test
        void testExportActiveUsersCsv() throws Exception {
                when(analyticsService.getMostActiveUsers(100)).thenReturn(List.of());
                when(exportService.exportActiveUsersToCsv(any())).thenReturn("csv content".getBytes());

                mockMvc.perform(get("/admin/reports/export/active-users")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(header().string("Content-Disposition",
                                                "attachment; filename=aktywni_uzytkownicy.csv"))
                                .andExpect(content().contentType("text/csv;charset=UTF-8"));

                verify(analyticsService).getMostActiveUsers(100);
                verify(exportService).exportActiveUsersToCsv(any());
        }

        // ==================== Users Tests ====================

        @Test
        void testListUsers() throws Exception {
                Page<UserResponseDto> userPage = new PageImpl<>(List.of());
                when(userService.findAll(any(Pageable.class))).thenReturn(userPage);

                mockMvc.perform(get("/admin/users")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/users"))
                                .andExpect(model().attributeExists("users"));

                verify(userService).findAll(any(Pageable.class));
        }

        @Test
        void testNewUserForm() throws Exception {
                mockMvc.perform(get("/admin/users/new")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/user-form"))
                                .andExpect(model().attributeExists("user"));
        }

        @Test
        void testCreateUserSuccess() throws Exception {
                when(userService.save(any(UserRequestDto.class)))
                                .thenReturn(UserResponseDto.builder().id(1L).username("newuser").build());

                mockMvc.perform(post("/admin/users")
                                .param("username", "newuser")
                                .param("password", "password123456")
                                .param("email", "new@example.com")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/users"))
                                .andExpect(flash().attributeExists("success"));

                verify(userService).save(any(UserRequestDto.class));
        }

        @Test
        void testCreateUserError() throws Exception {
                when(userService.save(any(UserRequestDto.class)))
                                .thenThrow(new RuntimeException("User already exists"));

                mockMvc.perform(post("/admin/users")
                                .param("username", "existinguser")
                                .param("password", "password123456")
                                .param("email", "existing@example.com")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/users"))
                                .andExpect(flash().attributeExists("error"));

                verify(userService).save(any(UserRequestDto.class));
        }

        @Test
        void testEditUserForm() throws Exception {
                UserResponseDto user = UserResponseDto.builder()
                                .id(1L)
                                .username("testuser")
                                .email("test@example.com")
                                .role(UserRole.USER)
                                .enabled(true)
                                .build();

                when(userService.findById(1L)).thenReturn(Optional.of(user));

                mockMvc.perform(get("/admin/users/1/edit")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/user-form"))
                                .andExpect(model().attributeExists("user"));

                verify(userService).findById(1L);
        }

        @Test
        void testUpdateUser() throws Exception {
                when(userService.partialUpdate(eq(1L), any(UserRequestDto.class)))
                                .thenReturn(UserResponseDto.builder().id(1L).username("updateduser").build());

                // Test update without password (e.g., just changing role)
                mockMvc.perform(post("/admin/users/1")
                                .param("username", "updateduser")
                                .param("email", "updated@example.com")
                                .param("role", "ADMIN")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/users"))
                                .andExpect(flash().attributeExists("success"));

                verify(userService).partialUpdate(eq(1L), any(UserRequestDto.class));
        }

        @Test
        void testUpdateUserError() throws Exception {
                when(userService.partialUpdate(eq(1L), any(UserRequestDto.class)))
                                .thenThrow(new RuntimeException("Update failed"));

                mockMvc.perform(post("/admin/users/1")
                                .param("username", "updateduser")
                                .param("email", "updated@example.com")
                                .param("role", "USER")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/users"))
                                .andExpect(flash().attributeExists("error"));

                verify(userService).partialUpdate(eq(1L), any(UserRequestDto.class));
        }

        @Test
        void testUpdateUserWithNewPassword() throws Exception {
                when(userService.partialUpdate(eq(1L), any(UserRequestDto.class)))
                                .thenReturn(UserResponseDto.builder().id(1L).username("updateduser").build());

                // Test update with new password
                mockMvc.perform(post("/admin/users/1")
                                .param("username", "updateduser")
                                .param("email", "updated@example.com")
                                .param("password", "newpassword12345")
                                .param("role", "ADMIN")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/users"))
                                .andExpect(flash().attributeExists("success"));

                verify(userService).partialUpdate(eq(1L), any(UserRequestDto.class));
        }

        @Test
        void testUpdateUserRoleChange() throws Exception {
                when(userService.partialUpdate(eq(1L), any(UserRequestDto.class)))
                                .thenReturn(UserResponseDto.builder().id(1L).username("testuser").role(UserRole.ADMIN)
                                                .build());

                // Test changing only the role without password
                mockMvc.perform(post("/admin/users/1")
                                .param("username", "testuser")
                                .param("email", "test@example.com")
                                .param("role", "ADMIN")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/users"))
                                .andExpect(flash().attributeExists("success"));

                verify(userService).partialUpdate(eq(1L), any(UserRequestDto.class));
        }

        @Test
        void testDeleteUserSuccess() throws Exception {
                doNothing().when(userService).delete(1L);

                mockMvc.perform(post("/admin/users/1/delete")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/users"))
                                .andExpect(flash().attributeExists("success"));

                verify(userService).delete(1L);
        }

        @Test
        void testDeleteUserError() throws Exception {
                doThrow(new RuntimeException("Cannot delete user"))
                                .when(userService).delete(1L);

                mockMvc.perform(post("/admin/users/1/delete")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/users"))
                                .andExpect(flash().attributeExists("error"));

                verify(userService).delete(1L);
        }

        // ==================== Validation Error Tests ====================

        @Test
        void testCreateBookValidationError() throws Exception {
                Page<AuthorDto> authorPage = new PageImpl<>(List.of());
                Page<GenreDto> genrePage = new PageImpl<>(List.of());

                when(authorService.findAll(any(Pageable.class))).thenReturn(authorPage);
                when(genreService.findAll(any(Pageable.class))).thenReturn(genrePage);

                // Missing required fields should trigger validation error
                mockMvc.perform(post("/admin/books")
                                .param("title", "") // empty title
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/book-form"));

                verify(bookService, never()).save(any(BookDto.class));
        }

        @Test
        void testCreateAuthorValidationError() throws Exception {
                mockMvc.perform(post("/admin/authors")
                                .param("name", "") // empty name
                                .param("lastName", "") // empty lastName
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/author-form"));

                verify(authorService, never()).save(any(AuthorDto.class));
        }

        @Test
        void testCreateGenreValidationError() throws Exception {
                mockMvc.perform(post("/admin/genres")
                                .param("name", "") // empty name
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/genre-form"));

                verify(genreService, never()).save(any(GenreDto.class));
        }

        @Test
        void testUpdateBookValidationError() throws Exception {
                Page<AuthorDto> authorPage = new PageImpl<>(List.of());
                Page<GenreDto> genrePage = new PageImpl<>(List.of());

                when(authorService.findAll(any(Pageable.class))).thenReturn(authorPage);
                when(genreService.findAll(any(Pageable.class))).thenReturn(genrePage);

                mockMvc.perform(post("/admin/books/1")
                                .param("title", "") // empty title
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/book-form"));

                verify(bookService, never()).save(any(BookDto.class));
        }

        @Test
        void testCreateUserValidationError() throws Exception {
                mockMvc.perform(post("/admin/users")
                                .param("username", "") // empty username
                                .param("password", "short") // short password
                                .param("email", "invalid-email") // invalid email
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/user-form"));

                verify(userService, never()).save(any(UserRequestDto.class));
        }

        @Test
        void testUpdateUserValidationError() throws Exception {
                // Empty username should cause validation error, but empty password should not
                mockMvc.perform(post("/admin/users/1")
                                .param("username", "") // empty username - validation error
                                .param("email", "valid@example.com") // valid email
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/user-form"));

                verify(userService, never()).partialUpdate(anyLong(), any(UserRequestDto.class));
        }

        // ==================== Cover Image Upload Error Test ====================

        @Test
        void testCreateBookWithCoverImageUploadError() throws Exception {
                Page<AuthorDto> authorPage = new PageImpl<>(List.of());
                Page<GenreDto> genrePage = new PageImpl<>(List.of());

                when(authorService.findAll(any(Pageable.class))).thenReturn(authorPage);
                when(genreService.findAll(any(Pageable.class))).thenReturn(genrePage);
                when(bookService.save(any(BookDto.class))).thenReturn(BookDto.builder().id(1L).build());
                when(fileStorageService.storeFile(any(), eq("covers")))
                                .thenThrow(new RuntimeException("Upload failed"));

                MockMultipartFile coverImage = new MockMultipartFile(
                                "coverImage", "test.jpg", "image/jpeg", "test image content".getBytes());

                mockMvc.perform(multipart("/admin/books")
                                .file(coverImage)
                                .param("title", "New Book")
                                .param("isbn", "978-0-123456-78-9")
                                .param("publishYear", "2021")
                                .param("copiesAvailable", "5")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/books"))
                                .andExpect(flash().attributeExists("error"));

                verify(fileStorageService).storeFile(any(), eq("covers"));
        }

        @Test
        void testUpdateBookWithNewCoverImage() throws Exception {
                BookDto existingBook = BookDto.builder().id(1L).title("Book").build();
                Page<AuthorDto> authorPage = new PageImpl<>(List.of());
                Page<GenreDto> genrePage = new PageImpl<>(List.of());

                when(bookService.findById(1L)).thenReturn(Optional.of(existingBook));
                when(authorService.findAll(any(Pageable.class))).thenReturn(authorPage);
                when(genreService.findAll(any(Pageable.class))).thenReturn(genrePage);
                when(bookService.save(any(BookDto.class))).thenReturn(existingBook);
                when(fileStorageService.storeFile(any(), eq("covers"))).thenReturn("covers/new.jpg");

                MockMultipartFile coverImage = new MockMultipartFile(
                                "coverImage", "new.jpg", "image/jpeg", "new image content".getBytes());

                mockMvc.perform(multipart("/admin/books/1")
                                .file(coverImage)
                                .param("title", "Updated Book")
                                .param("isbn", "978-0-123456-78-9")
                                .param("publishYear", "2021")
                                .param("copiesAvailable", "5")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/books"));

                verify(fileStorageService).storeFile(any(), eq("covers"));
                verify(bookService).save(any(BookDto.class));
        }

        @Test
        void testListBooksWithPagination() throws Exception {
                Page<BookDto> bookPage = new PageImpl<>(List.of());
                when(bookService.findAll(any(Pageable.class))).thenReturn(bookPage);

                mockMvc.perform(get("/admin/books")
                                .param("page", "2")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/books"));

                verify(bookService).findAll(any(Pageable.class));
        }

        @Test
        void testListReservationsWithPagination() throws Exception {
                Page<ReservationDto> reservationPage = new PageImpl<>(List.of());
                when(reservationService.findAll(any(Pageable.class))).thenReturn(reservationPage);

                mockMvc.perform(get("/admin/reservations")
                                .param("page", "3")
                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(view().name("admin/reservations"));

                verify(reservationService).findAll(any(Pageable.class));
        }
}
