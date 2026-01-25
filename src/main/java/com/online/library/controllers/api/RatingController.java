package com.online.library.controllers.api;

import com.online.library.domain.dto.RatingDto;
import com.online.library.services.RatingService;
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
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
@Tag(name = "Ratings", description = "API do zarządzania ocenami książek")
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    @Operation(summary = "Dodaj ocenę", description = "Dodaje nową ocenę dla książki (max 1 na tydzień per użytkownik)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ocena dodana", content = @Content(schema = @Schema(implementation = RatingDto.class))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe"),
            @ApiResponse(responseCode = "409", description = "Użytkownik już ocenił tę książkę w tym tygodniu")
    })
    public ResponseEntity<RatingDto> createRating(@Valid @RequestBody RatingDto ratingDto) {
        RatingDto savedRatingDto = ratingService.save(ratingDto);
        return new ResponseEntity<>(savedRatingDto, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Lista ocen", description = "Pobiera paginowaną listę wszystkich ocen")
    @ApiResponse(responseCode = "200", description = "Lista ocen")
    public Page<RatingDto> listRatings(Pageable pageable) {
        return ratingService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz ocenę", description = "Pobiera szczegóły oceny po ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ocena znaleziona", content = @Content(schema = @Schema(implementation = RatingDto.class))),
            @ApiResponse(responseCode = "404", description = "Ocena nie znaleziona")
    })
    public ResponseEntity<RatingDto> getRating(
            @Parameter(description = "ID oceny") @PathVariable("id") Long id) {
        return ratingService.findById(id)
                .map(ratingDto -> new ResponseEntity<>(ratingDto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aktualizuj ocenę", description = "Pełna aktualizacja oceny")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ocena zaktualizowana"),
            @ApiResponse(responseCode = "404", description = "Ocena nie znaleziona"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe")
    })
    public ResponseEntity<RatingDto> fullUpdateRating(
            @Parameter(description = "ID oceny") @PathVariable("id") Long id,
            @Valid @RequestBody RatingDto ratingDto) {
        if (!ratingService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ratingDto.setId(id);
        RatingDto savedRatingDto = ratingService.save(ratingDto);
        return new ResponseEntity<>(savedRatingDto, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Częściowa aktualizacja oceny", description = "Aktualizuje wybrane pola oceny")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ocena zaktualizowana"),
            @ApiResponse(responseCode = "404", description = "Ocena nie znaleziona")
    })
    public ResponseEntity<RatingDto> partialUpdateRating(
            @Parameter(description = "ID oceny") @PathVariable("id") Long id,
            @RequestBody RatingDto ratingDto) {
        if (!ratingService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        RatingDto updatedRating = ratingService.partialUpdate(id, ratingDto);
        return new ResponseEntity<>(updatedRating, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Usuń ocenę", description = "Usuwa ocenę z systemu")
    @ApiResponse(responseCode = "204", description = "Ocena usunięta")
    public ResponseEntity<Void> deleteRating(
            @Parameter(description = "ID oceny") @PathVariable("id") Long id) {
        ratingService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
