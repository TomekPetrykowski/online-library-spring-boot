package com.online.library.controllers;

import com.online.library.domain.dto.CommentDto;
import com.online.library.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto> createComment(@Valid @RequestBody CommentDto commentDto) {
        CommentDto savedCommentDto = commentService.save(commentDto);
        return new ResponseEntity<>(savedCommentDto, HttpStatus.CREATED);
    }

    @GetMapping
    public List<CommentDto> listComments() {
        return commentService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentDto> getComment(@PathVariable("id") Long id) {
        return commentService.findById(id)
                .map(commentDto -> new ResponseEntity<>(commentDto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDto> fullUpdateComment(@PathVariable("id") Long id, @Valid @RequestBody CommentDto commentDto) {
        if (!commentService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        commentDto.setId(id);
        CommentDto savedCommentDto = commentService.save(commentDto);
        return new ResponseEntity<>(savedCommentDto, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommentDto> partialUpdateComment(@PathVariable("id") Long id, @RequestBody CommentDto commentDto) {
        if (!commentService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        CommentDto updatedComment = commentService.partialUpdate(id, commentDto);
        return new ResponseEntity<>(updatedComment, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable("id") Long id) {
        commentService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
