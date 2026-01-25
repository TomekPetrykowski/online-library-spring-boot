package com.online.library.controllers.api;

import com.online.library.domain.dto.BookDto;
import com.online.library.services.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "API do zarządzania książkami")
public class BookController {

    private final BookService bookService;

    @PostMapping
    @Operation(summary = "Utwórz nową książkę", description = "Dodaje nową książkę do systemu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Książka utworzona", content = @Content(schema = @Schema(implementation = BookDto.class))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe")
    })
    public ResponseEntity<BookDto> createBook(@Valid @RequestBody BookDto bookDto) {
        BookDto savedBookDto = bookService.save(bookDto);
        return new ResponseEntity<>(savedBookDto, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Lista książek", description = "Pobiera paginowaną listę wszystkich książek")
    @ApiResponse(responseCode = "200", description = "Lista książek")
    public Page<BookDto> listBooks(Pageable pageable) {
        return bookService.findAll(pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Wyszukaj książki", description = "Wyszukuje książki po tytule, autorze lub gatunku")
    @ApiResponse(responseCode = "200", description = "Wyniki wyszukiwania")
    public Page<BookDto> searchBooks(
            @Parameter(description = "Fraza wyszukiwania") @RequestParam(name = "q", required = false) String searchTerm,
            Pageable pageable) {
        return bookService.searchBooks(searchTerm, pageable);
    }

    @GetMapping("/popular")
    @Operation(summary = "Popularne książki", description = "Pobiera listę najpopularniejszych książek według ocen")
    @ApiResponse(responseCode = "200", description = "Lista popularnych książek")
    public Page<BookDto> getPopularBooks(Pageable pageable) {
        return bookService.getPopularBooks(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz książkę", description = "Pobiera szczegóły książki po ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Książka znaleziona", content = @Content(schema = @Schema(implementation = BookDto.class))),
            @ApiResponse(responseCode = "404", description = "Książka nie znaleziona")
    })
    public ResponseEntity<BookDto> getBook(
            @Parameter(description = "ID książki") @PathVariable("id") Long id) {
        return bookService.findById(id)
                .map(bookDto -> new ResponseEntity<>(bookDto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aktualizuj książkę", description = "Pełna aktualizacja książki")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Książka zaktualizowana"),
            @ApiResponse(responseCode = "404", description = "Książka nie znaleziona"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe")
    })
    public ResponseEntity<BookDto> fullUpdateBook(
            @Parameter(description = "ID książki") @PathVariable("id") Long id,
            @Valid @RequestBody BookDto bookDto) {
        if (!bookService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        bookDto.setId(id);
        BookDto savedBookDto = bookService.save(bookDto);
        return new ResponseEntity<>(savedBookDto, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Częściowa aktualizacja książki", description = "Aktualizuje wybrane pola książki")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Książka zaktualizowana"),
            @ApiResponse(responseCode = "404", description = "Książka nie znaleziona")
    })
    public ResponseEntity<BookDto> partialUpdateBook(
            @Parameter(description = "ID książki") @PathVariable("id") Long id,
            @RequestBody BookDto bookDto) {
        if (!bookService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        BookDto updatedBook = bookService.partialUpdate(id, bookDto);
        return new ResponseEntity<>(updatedBook, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Usuń książkę", description = "Usuwa książkę z systemu")
    @ApiResponse(responseCode = "204", description = "Książka usunięta")
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID książki") @PathVariable("id") Long id) {
        bookService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
