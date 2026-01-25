package com.online.library.controllers.api;

import com.online.library.domain.dto.GenreDto;
import com.online.library.services.GenreService;
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
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
@Tag(name = "Genres", description = "API do zarządzania gatunkami literackimi")
public class GenreController {

    private final GenreService genreService;

    @PostMapping
    @Operation(summary = "Utwórz gatunek", description = "Dodaje nowy gatunek literacki")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Gatunek utworzony", content = @Content(schema = @Schema(implementation = GenreDto.class))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe")
    })
    public ResponseEntity<GenreDto> createGenre(@Valid @RequestBody GenreDto genreDto) {
        GenreDto savedGenreDto = genreService.save(genreDto);
        return new ResponseEntity<>(savedGenreDto, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Lista gatunków", description = "Pobiera paginowaną listę wszystkich gatunków")
    @ApiResponse(responseCode = "200", description = "Lista gatunków")
    public Page<GenreDto> listGenres(Pageable pageable) {
        return genreService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz gatunek", description = "Pobiera szczegóły gatunku po ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gatunek znaleziony", content = @Content(schema = @Schema(implementation = GenreDto.class))),
            @ApiResponse(responseCode = "404", description = "Gatunek nie znaleziony")
    })
    public ResponseEntity<GenreDto> getGenre(
            @Parameter(description = "ID gatunku") @PathVariable("id") Long id) {
        return genreService.findById(id)
                .map(genreDto -> new ResponseEntity<>(genreDto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aktualizuj gatunek", description = "Pełna aktualizacja gatunku")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gatunek zaktualizowany"),
            @ApiResponse(responseCode = "404", description = "Gatunek nie znaleziony"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe")
    })
    public ResponseEntity<GenreDto> fullUpdateGenre(
            @Parameter(description = "ID gatunku") @PathVariable("id") Long id,
            @Valid @RequestBody GenreDto genreDto) {
        if (!genreService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        genreDto.setId(id);
        GenreDto savedGenreDto = genreService.save(genreDto);
        return new ResponseEntity<>(savedGenreDto, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Częściowa aktualizacja gatunku", description = "Aktualizuje wybrane pola gatunku")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gatunek zaktualizowany"),
            @ApiResponse(responseCode = "404", description = "Gatunek nie znaleziony")
    })
    public ResponseEntity<GenreDto> partialUpdateGenre(
            @Parameter(description = "ID gatunku") @PathVariable("id") Long id,
            @RequestBody GenreDto genreDto) {
        if (!genreService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        GenreDto updatedGenre = genreService.partialUpdate(id, genreDto);
        return new ResponseEntity<>(updatedGenre, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Usuń gatunek", description = "Usuwa gatunek z systemu")
    @ApiResponse(responseCode = "204", description = "Gatunek usunięty")
    public ResponseEntity<Void> deleteGenre(
            @Parameter(description = "ID gatunku") @PathVariable("id") Long id) {
        genreService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
