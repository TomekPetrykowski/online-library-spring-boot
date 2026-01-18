package com.online.library.controllers;

import com.online.library.domain.dto.GenreDto;
import com.online.library.services.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @PostMapping
    public ResponseEntity<GenreDto> createGenre(@Valid @RequestBody GenreDto genreDto) {
        GenreDto savedGenreDto = genreService.save(genreDto);
        return new ResponseEntity<>(savedGenreDto, HttpStatus.CREATED);
    }

    @GetMapping
    public Page<GenreDto> listGenres(Pageable pageable) {
        return genreService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreDto> getGenre(@PathVariable("id") Long id) {
        return genreService.findById(id)
                .map(genreDto -> new ResponseEntity<>(genreDto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenreDto> fullUpdateGenre(@PathVariable("id") Long id, @Valid @RequestBody GenreDto genreDto) {
        if (!genreService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        genreDto.setId(id);
        GenreDto savedGenreDto = genreService.save(genreDto);
        return new ResponseEntity<>(savedGenreDto, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GenreDto> partialUpdateGenre(@PathVariable("id") Long id, @RequestBody GenreDto genreDto) {
        if (!genreService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        GenreDto updatedGenre = genreService.partialUpdate(id, genreDto);
        return new ResponseEntity<>(updatedGenre, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable("id") Long id) {
        genreService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
