package com.online.library.controllers;

import com.online.library.domain.dto.RatingDto;
import com.online.library.services.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<RatingDto> createRating(@Valid @RequestBody RatingDto ratingDto) {
        RatingDto savedRatingDto = ratingService.save(ratingDto);
        return new ResponseEntity<>(savedRatingDto, HttpStatus.CREATED);
    }

    @GetMapping
    public List<RatingDto> listRatings() {
        return ratingService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RatingDto> getRating(@PathVariable("id") Long id) {
        return ratingService.findById(id)
                .map(ratingDto -> new ResponseEntity<>(ratingDto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RatingDto> fullUpdateRating(@PathVariable("id") Long id, @Valid @RequestBody RatingDto ratingDto) {
        if (!ratingService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ratingDto.setId(id);
        RatingDto savedRatingDto = ratingService.save(ratingDto);
        return new ResponseEntity<>(savedRatingDto, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RatingDto> partialUpdateRating(@PathVariable("id") Long id, @RequestBody RatingDto ratingDto) {
        if (!ratingService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        RatingDto updatedRating = ratingService.partialUpdate(id, ratingDto);
        return new ResponseEntity<>(updatedRating, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable("id") Long id) {
        ratingService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
