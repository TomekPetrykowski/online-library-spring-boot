package com.online.library.controllers.api;

import com.online.library.domain.dto.AuthorDto;
import com.online.library.services.AuthorService;
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
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@Tag(name = "Authors", description = "API do zarządzania autorami")
public class AuthorController {

    private final AuthorService authorService;

    @PostMapping
    @Operation(summary = "Utwórz nowego autora", description = "Dodaje nowego autora do systemu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Autor utworzony", content = @Content(schema = @Schema(implementation = AuthorDto.class))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe")
    })
    public ResponseEntity<AuthorDto> createAuthor(@Valid @RequestBody AuthorDto authorDto) {
        AuthorDto savedAuthorDto = authorService.save(authorDto);
        return new ResponseEntity<>(savedAuthorDto, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Lista autorów", description = "Pobiera listę wszystkich autorów")
    @ApiResponse(responseCode = "200", description = "Lista autorów")
    public Page<AuthorDto> listAuthors(Pageable pageable) {
        return authorService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz autora", description = "Pobiera szczegóły autora po ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autor znaleziony", content = @Content(schema = @Schema(implementation = AuthorDto.class))),
            @ApiResponse(responseCode = "404", description = "Autor nie znaleziony")
    })
    public ResponseEntity<AuthorDto> getAuthor(
            @Parameter(description = "ID autora") @PathVariable("id") Long id) {
        return authorService.findById(id)
                .map(authorDto -> new ResponseEntity<>(authorDto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aktualizuj autora", description = "Pełna aktualizacja autora")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autor zaktualizowany"),
            @ApiResponse(responseCode = "404", description = "Autor nie znaleziony"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe")
    })
    public ResponseEntity<AuthorDto> fullUpdateAuthor(
            @Parameter(description = "ID autora") @PathVariable("id") Long id,
            @Valid @RequestBody AuthorDto authorDto) {
        if (!authorService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        authorDto.setId(id);
        AuthorDto savedAuthorDto = authorService.save(authorDto);
        return new ResponseEntity<>(savedAuthorDto, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Częściowa aktualizacja autora", description = "Aktualizuje wybrane pola autora")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autor zaktualizowany"),
            @ApiResponse(responseCode = "404", description = "Autor nie znaleziony")
    })
    public ResponseEntity<AuthorDto> partialUpdateAuthor(
            @Parameter(description = "ID autora") @PathVariable("id") Long id,
            @RequestBody AuthorDto authorDto) {
        if (!authorService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        AuthorDto updatedAuthor = authorService.partialUpdate(id, authorDto);
        return new ResponseEntity<>(updatedAuthor, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Usuń autora", description = "Usuwa autora z systemu")
    @ApiResponse(responseCode = "204", description = "Autor usunięty")
    public ResponseEntity<Void> deleteAuthor(
            @Parameter(description = "ID autora") @PathVariable("id") Long id) {
        authorService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
