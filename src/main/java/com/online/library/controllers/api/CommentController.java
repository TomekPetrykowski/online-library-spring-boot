package com.online.library.controllers.api;

import com.online.library.domain.dto.CommentDto;
import com.online.library.services.CommentService;
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
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "API do zarządzania komentarzami")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "Dodaj komentarz", description = "Dodaje nowy komentarz do książki")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Komentarz dodany", content = @Content(schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe")
    })
    public ResponseEntity<CommentDto> createComment(@Valid @RequestBody CommentDto commentDto) {
        CommentDto savedCommentDto = commentService.save(commentDto);
        return new ResponseEntity<>(savedCommentDto, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Lista komentarzy", description = "Pobiera paginowaną listę wszystkich komentarzy")
    @ApiResponse(responseCode = "200", description = "Lista komentarzy")
    public Page<CommentDto> listComments(Pageable pageable) {
        return commentService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz komentarz", description = "Pobiera szczegóły komentarza po ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Komentarz znaleziony", content = @Content(schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "404", description = "Komentarz nie znaleziony")
    })
    public ResponseEntity<CommentDto> getComment(
            @Parameter(description = "ID komentarza") @PathVariable("id") Long id) {
        return commentService.findById(id)
                .map(commentDto -> new ResponseEntity<>(commentDto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aktualizuj komentarz", description = "Pełna aktualizacja komentarza")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Komentarz zaktualizowany"),
            @ApiResponse(responseCode = "404", description = "Komentarz nie znaleziony"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe")
    })
    public ResponseEntity<CommentDto> fullUpdateComment(
            @Parameter(description = "ID komentarza") @PathVariable("id") Long id,
            @Valid @RequestBody CommentDto commentDto) {
        if (!commentService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        commentDto.setId(id);
        CommentDto savedCommentDto = commentService.save(commentDto);
        return new ResponseEntity<>(savedCommentDto, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Częściowa aktualizacja komentarza", description = "Aktualizuje wybrane pola komentarza")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Komentarz zaktualizowany"),
            @ApiResponse(responseCode = "404", description = "Komentarz nie znaleziony")
    })
    public ResponseEntity<CommentDto> partialUpdateComment(
            @Parameter(description = "ID komentarza") @PathVariable("id") Long id,
            @RequestBody CommentDto commentDto) {
        if (!commentService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        CommentDto updatedComment = commentService.partialUpdate(id, commentDto);
        return new ResponseEntity<>(updatedComment, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Usuń komentarz", description = "Usuwa komentarz z systemu")
    @ApiResponse(responseCode = "204", description = "Komentarz usunięty")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "ID komentarza") @PathVariable("id") Long id) {
        commentService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
