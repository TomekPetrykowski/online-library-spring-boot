package com.online.library.controllers.api;

import com.online.library.domain.dto.ReservationDto;
import com.online.library.services.ReservationService;
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
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "API do zarządzania rezerwacjami książek")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Utwórz rezerwację", description = "Tworzy nową rezerwację książki")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rezerwacja utworzona", content = @Content(schema = @Schema(implementation = ReservationDto.class))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe"),
            @ApiResponse(responseCode = "409", description = "Brak dostępnych egzemplarzy")
    })
    public ResponseEntity<ReservationDto> createReservation(@Valid @RequestBody ReservationDto reservationDto) {
        ReservationDto savedReservationDto = reservationService.save(reservationDto);
        return new ResponseEntity<>(savedReservationDto, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Lista rezerwacji", description = "Pobiera paginowaną listę wszystkich rezerwacji")
    @ApiResponse(responseCode = "200", description = "Lista rezerwacji")
    public Page<ReservationDto> listReservations(Pageable pageable) {
        return reservationService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz rezerwację", description = "Pobiera szczegóły rezerwacji po ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rezerwacja znaleziona", content = @Content(schema = @Schema(implementation = ReservationDto.class))),
            @ApiResponse(responseCode = "404", description = "Rezerwacja nie znaleziona")
    })
    public ResponseEntity<ReservationDto> getReservation(
            @Parameter(description = "ID rezerwacji") @PathVariable("id") Long id) {
        return reservationService.findById(id)
                .map(reservationDto -> new ResponseEntity<>(reservationDto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aktualizuj rezerwację", description = "Pełna aktualizacja rezerwacji")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rezerwacja zaktualizowana"),
            @ApiResponse(responseCode = "404", description = "Rezerwacja nie znaleziona"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe")
    })
    public ResponseEntity<ReservationDto> fullUpdateReservation(
            @Parameter(description = "ID rezerwacji") @PathVariable("id") Long id,
            @Valid @RequestBody ReservationDto reservationDto) {
        if (!reservationService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        reservationDto.setId(id);
        ReservationDto savedReservationDto = reservationService.save(reservationDto);
        return new ResponseEntity<>(savedReservationDto, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Częściowa aktualizacja rezerwacji", description = "Aktualizuje wybrane pola rezerwacji")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rezerwacja zaktualizowana"),
            @ApiResponse(responseCode = "404", description = "Rezerwacja nie znaleziona")
    })
    public ResponseEntity<ReservationDto> partialUpdateReservation(
            @Parameter(description = "ID rezerwacji") @PathVariable("id") Long id,
            @RequestBody ReservationDto reservationDto) {
        if (!reservationService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ReservationDto updatedReservation = reservationService.partialUpdate(id, reservationDto);
        return new ResponseEntity<>(updatedReservation, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Usuń rezerwację", description = "Usuwa rezerwację z systemu")
    @ApiResponse(responseCode = "204", description = "Rezerwacja usunięta")
    public ResponseEntity<Void> deleteReservation(
            @Parameter(description = "ID rezerwacji") @PathVariable("id") Long id) {
        reservationService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
