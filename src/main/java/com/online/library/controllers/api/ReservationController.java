package com.online.library.controllers.api;

import com.online.library.domain.dto.ReservationDto;
import com.online.library.services.ReservationService;
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
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationDto> createReservation(@Valid @RequestBody ReservationDto reservationDto) {
        ReservationDto savedReservationDto = reservationService.save(reservationDto);
        return new ResponseEntity<>(savedReservationDto, HttpStatus.CREATED);
    }

    @GetMapping
    public Page<ReservationDto> listReservations(Pageable pageable) {
        return reservationService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto> getReservation(@PathVariable("id") Long id) {
        return reservationService.findById(id)
                .map(reservationDto -> new ResponseEntity<>(reservationDto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationDto> fullUpdateReservation(@PathVariable("id") Long id, @Valid @RequestBody ReservationDto reservationDto) {
        if (!reservationService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        reservationDto.setId(id);
        ReservationDto savedReservationDto = reservationService.save(reservationDto);
        return new ResponseEntity<>(savedReservationDto, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationDto> partialUpdateReservation(@PathVariable("id") Long id, @RequestBody ReservationDto reservationDto) {
        if (!reservationService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ReservationDto updatedReservation = reservationService.partialUpdate(id, reservationDto);
        return new ResponseEntity<>(updatedReservation, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") Long id) {
        reservationService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
