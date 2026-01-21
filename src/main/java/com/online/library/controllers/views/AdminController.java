package com.online.library.controllers.views;

import com.online.library.domain.dto.*;
import com.online.library.domain.enums.ReservationStatus;
import com.online.library.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final Pageable ALL_ITEMS = PageRequest.of(0, 1000);

    private final BookService bookService;
    private final AuthorService authorService;
    private final GenreService genreService;
    private final ReservationService reservationService;
    private final AnalyticsService analyticsService;

    @GetMapping
    public String adminDashboard(Model model) {
        return "admin/dashboard";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("popularBooks", analyticsService.getMostPopularBooks(10));
        model.addAttribute("readAuthors", analyticsService.getMostReadAuthors(10));
        model.addAttribute("activeUsers", analyticsService.getMostActiveUsers(10));
        return "admin/reports";
    }

    @GetMapping("/reservations")
    public String listReservations(
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        Pageable pageable = PageRequest.of(page, 20);
        Page<ReservationDto> reservations = reservationService.findAll(pageable);
        model.addAttribute("reservations", reservations);
        model.addAttribute("statuses", ReservationStatus.values());
        return "admin/reservations";
    }

    @PostMapping("/reservations/{id}/status")
    public String changeReservationStatus(
            @PathVariable Long id,
            @RequestParam ReservationStatus status,
            RedirectAttributes redirectAttributes) {
        try {
            reservationService.changeStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Status rezerwacji zmieniony na: " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    @GetMapping("/books")
    public String listBooks(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 20);
        model.addAttribute("books", bookService.findAll(pageable));
        return "admin/books";
    }

    @GetMapping("/books/new")
    public String newBookForm(Model model) {
        model.addAttribute("book", new BookDto());
        model.addAttribute("allAuthors", authorService.findAll(ALL_ITEMS).getContent());
        model.addAttribute("allGenres", genreService.findAll(ALL_ITEMS).getContent());
        return "admin/book-form";
    }

    @PostMapping("/books")
    public String createBook(@Valid @ModelAttribute("book") BookDto bookDto,
            BindingResult result,
            @RequestParam(value = "authorIds", required = false) List<Long> authorIds,
            @RequestParam(value = "genreIds", required = false) List<Long> genreIds,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("allAuthors", authorService.findAll(ALL_ITEMS).getContent());
            model.addAttribute("allGenres", genreService.findAll(ALL_ITEMS).getContent());
            return "admin/book-form";
        }

        // Set authors
        if (authorIds != null && !authorIds.isEmpty()) {
            Set<AuthorDto> authors = new HashSet<>();
            for (Long authorId : authorIds) {
                authorService.findById(authorId).ifPresent(authors::add);
            }
            bookDto.setAuthors(authors);
        }

        // Set genres
        if (genreIds != null && !genreIds.isEmpty()) {
            Set<GenreDto> genres = new HashSet<>();
            for (Long genreId : genreIds) {
                genreService.findById(genreId).ifPresent(genres::add);
            }
            bookDto.setGenres(genres);
        }

        bookService.save(bookDto);
        redirectAttributes.addFlashAttribute("success", "Książka dodana.");
        return "redirect:/admin/books";
    }

    @GetMapping("/books/{id}/edit")
    public String editBookForm(@PathVariable Long id, Model model) {
        BookDto book = bookService.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        model.addAttribute("book", book);
        model.addAttribute("allAuthors", authorService.findAll(ALL_ITEMS).getContent());
        model.addAttribute("allGenres", genreService.findAll(ALL_ITEMS).getContent());
        return "admin/book-form";
    }

    @PostMapping("/books/{id}")
    public String updateBook(@PathVariable Long id,
            @Valid @ModelAttribute("book") BookDto bookDto,
            BindingResult result,
            @RequestParam(value = "authorIds", required = false) List<Long> authorIds,
            @RequestParam(value = "genreIds", required = false) List<Long> genreIds,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("allAuthors", authorService.findAll(ALL_ITEMS).getContent());
            model.addAttribute("allGenres", genreService.findAll(ALL_ITEMS).getContent());
            return "admin/book-form";
        }
        bookDto.setId(id);

        // Set authors
        if (authorIds != null && !authorIds.isEmpty()) {
            Set<AuthorDto> authors = new HashSet<>();
            for (Long authorId : authorIds) {
                authorService.findById(authorId).ifPresent(authors::add);
            }
            bookDto.setAuthors(authors);
        } else {
            bookDto.setAuthors(new HashSet<>());
        }

        // Set genres
        if (genreIds != null && !genreIds.isEmpty()) {
            Set<GenreDto> genres = new HashSet<>();
            for (Long genreId : genreIds) {
                genreService.findById(genreId).ifPresent(genres::add);
            }
            bookDto.setGenres(genres);
        } else {
            bookDto.setGenres(new HashSet<>());
        }

        bookService.save(bookDto);
        redirectAttributes.addFlashAttribute("success", "Książka zaktualizowana.");
        return "redirect:/admin/books";
    }

    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Książka usunięta.");
        return "redirect:/admin/books";
    }

    @GetMapping("/authors")
    public String listAuthors(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 20);
        model.addAttribute("authors", authorService.findAll(pageable));
        return "admin/authors";
    }

    @GetMapping("/authors/new")
    public String newAuthorForm(Model model) {
        model.addAttribute("author", new AuthorDto());
        return "admin/author-form";
    }

    @PostMapping("/authors")
    public String createAuthor(@Valid @ModelAttribute("author") AuthorDto authorDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/author-form";
        }
        authorService.save(authorDto);
        redirectAttributes.addFlashAttribute("success", "Autor dodany.");
        return "redirect:/admin/authors";
    }

    @PostMapping("/authors/{id}/delete")
    public String deleteAuthor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        authorService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Autor usunięty.");
        return "redirect:/admin/authors";
    }

    @GetMapping("/genres")
    public String listGenres(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 20);
        model.addAttribute("genres", genreService.findAll(pageable));
        return "admin/genres";
    }

    @GetMapping("/genres/new")
    public String newGenreForm(Model model) {
        model.addAttribute("genre", new GenreDto());
        return "admin/genre-form";
    }

    @PostMapping("/genres")
    public String createGenre(@Valid @ModelAttribute("genre") GenreDto genreDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/genre-form";
        }
        genreService.save(genreDto);
        redirectAttributes.addFlashAttribute("success", "Gatunek dodany.");
        return "redirect:/admin/genres";
    }

    @PostMapping("/genres/{id}/delete")
    public String deleteGenre(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        genreService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Gatunek usunięty.");
        return "redirect:/admin/genres";
    }
}