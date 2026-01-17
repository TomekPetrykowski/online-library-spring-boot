package com.online.library.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDto {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    private String description;

    @Size(max = 100, message = "Publisher must be at most 100 characters")
    private String publisher;

    @NotNull(message = "Publish year is required")
    private Integer publishYear;

    @NotBlank(message = "ISBN is required")
    @Size(max = 20, message = "ISBN must be at most 20 characters")
    private String isbn;

    private String coverImagePath;

    private Integer copiesAvailable;

    private BigDecimal averageRating;

    private LocalDateTime createdAt;

    private Set<AuthorDto> authors;

    private Set<GenreDto> genres;
}
