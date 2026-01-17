package com.online.library.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {

    private Long id;

    @NotNull(message = "User is required")
    private UserResponseDto user;

    @NotNull(message = "Book is required")
    private BookDto book;

    @NotBlank(message = "Content is required")
    private String content;

    private LocalDateTime createdAt;
}
