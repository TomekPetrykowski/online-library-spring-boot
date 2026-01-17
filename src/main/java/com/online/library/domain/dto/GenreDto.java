package com.online.library.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenreDto {

    private Long id;

    @NotBlank(message = "Genre name is required")
    @Size(max = 50, message = "Genre name must be at most 50 characters")
    private String name;
}
